#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo_util

INPUT_DEVICES = "input/devices.json"
INPUT_SWITCHES = "input/switches.json"
INPUT_ISIS = "input/isis.json"
INPUT_PORTS = "input/ports.json"
INPUT_ADDRS = "input/ip_addrs.json"

OUTPUT_DEVICES = "output/devices.json"
OUTPUT_ADJCIES = "output/adjacencies.json"
OUTPUT_ADDRS = "output/addrs.json"


def main():
    pp = pprint.PrettyPrinter(indent=4)

    in_str = open(INPUT_DEVICES).read()
    in_devices = json.loads(in_str)

    in_str = open(INPUT_ISIS).read()
    isis_adjcies = json.loads(in_str)

    in_str = open(INPUT_PORTS).read()
    in_ports = json.loads(in_str)

    in_str = open(INPUT_ADDRS).read()
    addrs = json.loads(in_str)


    oscars_devices = transform_devices(in_devices=in_devices)

    (oscars_adjcies, igp_portmap) = transform_isis(isis_adjcies=isis_adjcies)

    filter_out_not_igp(igp_portmap=igp_portmap, oscars_devices=oscars_devices)

    merge_isis_ports(oscars_devices=oscars_devices, igp_portmap=igp_portmap)

    merge_phy_ports(oscars_devices=oscars_devices, ports=in_ports, igp_portmap=igp_portmap)

    urn_addrs = make_urn_addrs(addrs=addrs, isis_adjcies=isis_adjcies)

    with open(OUTPUT_DEVICES, 'w') as outfile:
        json.dump(oscars_devices, outfile, indent=2)

    with open(OUTPUT_ADJCIES, 'w') as outfile:
        json.dump(oscars_adjcies, outfile, indent=2)

    with open(OUTPUT_ADDRS, 'w') as outfile:
        json.dump(urn_addrs, outfile, indent=2)


def make_urn_addrs(addrs=None, isis_adjcies=None):
    urn_addrs_dict = {}
    for addr in addrs:
        int_name = addr["int_name"]
        address = addr["address"]
        router = addr["router"]

        if int_name == "lo0.0" or int_name == "system":
            urn = router
            urn_addrs_dict[urn] = address
    for isis_adjcy in isis_adjcies:
        address = isis_adjcy["a_addr"]
        router = isis_adjcy["a"]
        port = isis_adjcy["a_port"]
        urn = router+":"+port
        urn_addrs_dict[urn] = address

    urn_addrs = []
    for urn in urn_addrs_dict.keys():
        entry = {
            "urn": urn,
            "ipv4Address": urn_addrs_dict[urn]
        }
        urn_addrs.append(entry)


    return urn_addrs



def filter_out_not_igp(igp_portmap=None, oscars_devices=None):
    remove_these = []
    for device in oscars_devices:
        device_name = device["urn"]
        if device_name not in igp_portmap.keys():
            remove_these.append(device)

    for device in remove_these:
        oscars_devices.remove(device)


def merge_phy_ports(ports=None, oscars_devices=None, igp_portmap=None):
    for device_name in ports.keys():
        for device in oscars_devices:
            if device["urn"] == device_name:
                for port in ports[device_name].keys():
                    port_ifces = ports[device_name][port]
                    mbps = 0
                    for ifce_data in port_ifces:
                        mbps = ifce_data["mbps"]

                    port_in_igp = False
                    if device_name in igp_portmap.keys():
                        if port in igp_portmap[device_name].keys():
                            port_in_igp = True

                    ifce_data = {
                        "urn": device_name + ":" + port,
                        "reservableBw": mbps
                    }

                    if port_in_igp:
                        ifce_data["capabilities"] = ["MPLS"]
                    else:
                        ifce_data["capabilities"] = ["ETHERNET"]
                        ifce_data["reservableVlans"] = [
                            {
                                "floor": 2000,
                                "ceiling": 2999
                            }
                        ]

                    device["ifces"].append(ifce_data)


def merge_isis_ports(oscars_devices=None, igp_portmap=None):
    for device_name in igp_portmap.keys():
        found_device = False
        for device in oscars_devices:
            if device["urn"] == device_name:
                found_device = True
                for port_name in igp_portmap[device_name].keys():
                    mbps = igp_portmap[device_name][port_name]["mbps"]
                    port_urn = device_name + ":" + port_name
                    found_port = False
                    for ifce_data in device["ifces"]:
                        if ifce_data["urn"] == port_urn:
                            found_port = True
                            if "MPLS" not in ifce_data["capabilities"]:
                                ifce_data["capabilities"].append("MPLS")

                    if not found_port:
                        new_ifce_data = {
                            "urn": port_urn,
                            "capabilities": ["MPLS"],
                            "reservableBw": mbps
                        }
                        device["ifces"].append(new_ifce_data)
        if not found_device:
            raise ValueError("can't find device %s" % device_name)


def transform_devices(in_devices=None):
    out_routers = []
    for rs in in_devices:
        model = model_map(os=rs["os"], description=rs["description"])
        out_router = {
            "urn": rs["name"],
            "model": model,
            "type": "ROUTER",
            "capabilities": ["ETHERNET", "MPLS"],
            "ifces": [],
            "reservableVlans": []
        }
        out_routers.append(out_router)

    return out_routers


def model_map(os=None, description=None):
    if description == "Alcatel":
        return "ALCATEL_SR7750"
    elif description == "Juniper":
        parts = str(os).split(" ")
        model = "JUNIPER_MX"
        #        model = "JUNIPER_" + str(parts[0]).upper()
        return model
    else:
        raise ValueError("could not decide router model for [%s] [%s]" % (os, description))


def transform_isis(isis_adjcies=None):
    oscars_adjcies = []
    igp_portmap = {}
    for isis_adjcy in isis_adjcies:
        router_a = isis_adjcy["a"]
        router_z = isis_adjcy["z"]
        port_a = isis_adjcy["a_port"]
        port_z = isis_adjcy["z_port"]
        a_urn = router_a + ":" + port_a
        z_urn = router_z + ":" + port_z
        oscars_adjcy = {
            "a": a_urn,
            "z": z_urn,
            "metrics": {
                "MPLS": isis_adjcy["latency"]
            }
        }
        oscars_adjcies.append(oscars_adjcy)

        if router_a not in igp_portmap.keys():
            igp_portmap[router_a] = {}

        igp_portmap[router_a][port_a] = {
            "mbps": isis_adjcy["mbps"]
        }
    return oscars_adjcies, igp_portmap


if __name__ == '__main__':
    main()
