#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo_util

INPUT_DEVICES = "input/devices.json"
INPUT_SWITCHES = "input/switches.json"
INPUT_EDGES = "input/edges.json"
INPUT_ISIS = "input/isis.json"

OUTPUT_DEVICES = "output/devices.json"
OUTPUT_ADJCIES = "output/adjacencies.json"


def main():
    pp = pprint.PrettyPrinter(indent=4)

    oscars_devices = []
    oscars_adjcies = []

    in_str = open(INPUT_DEVICES).read()
    in_devices = json.loads(in_str)

    oscars_devices = transform_devices(in_devices=in_devices)

    in_str = open(INPUT_ISIS).read()
    isis_adjcies = json.loads(in_str)

    (oscars_adjcies, igp_portmap) = transform_isis(isis_adjcies=isis_adjcies)

    merge_isis_ports(devices=oscars_devices, igp_portmap=igp_portmap)

    with open(OUTPUT_DEVICES, 'w') as outfile:
        json.dump(oscars_devices, outfile, indent=2)

    with open(OUTPUT_ADJCIES, 'w') as outfile:
        json.dump(oscars_adjcies, outfile, indent=2)


def merge_isis_ports(devices=None, igp_portmap=None):
    for device_name in igp_portmap.keys():
        found_device = False
        for device in devices:
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
