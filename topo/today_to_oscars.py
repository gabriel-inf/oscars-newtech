#!/usr/bin/env python
# encoding: utf-8

import json
import pprint
import topo_util

INPUT_TODAY = "input/today.json"
INPUT_SWITCHES = "input/switches.json"
INPUT_EDGES = "input/edges.json"
OUTPUT_DEVICES = "output/devices.json"
OUTPUT_ADJCIES = "output/adjacencies.json"


def main():
    pp = pprint.PrettyPrinter(indent=4)

    in_str = open(INPUT_TODAY).read()
    today = json.loads(in_str)

    in_str = open(INPUT_SWITCHES).read()
    packed_switches = json.loads(in_str)

    in_str = open(INPUT_EDGES).read()
    packed_edges = json.loads(in_str)

    ipv4nets = today["today"]["ipv4net"]
    latency_db = today["latency"]
    router_system = today["today"]["router_system"]
    vlans = today["today"]["VLAN"]

    devices = today_routers(router_system=router_system)
    (today_ports, interfaces) = today_ports_from_vlans(vlans=vlans)
    today_ports = today_ports_from_ipv4nets(ipv4nets=ipv4nets, ports=today_ports)

    (unpacked_switches, uplink_adjcies) = unpack_switches(packed_switches=packed_switches)

    (unpacked_edges, downlink_adjcies) = unpack_edges(packed_edges=packed_edges)

    merge_edges(devices=devices, edges=unpacked_edges)
    merge_switches(devices=devices, switches=unpacked_switches)
    merge_all_ports(ports=today_ports, devices=devices)

    isis_neighbors = get_isis_neighbors(ipv4nets=ipv4nets, latency_db=latency_db)
    (nodes, isis_adcjies) = make_isis_graph(isis=isis_neighbors)
    (oscars_adjcies, isis_ports) = transform_adjcies(isis_adcjies=isis_adcjies)
    merge_up_downlink_adjcies(oscars_adjcies=oscars_adjcies,
                              uplink_adjcies=uplink_adjcies,
                              downlink_adjcies=downlink_adjcies)

    merge_isis_ports(devices=devices, isis_ports=isis_ports)

    with open(OUTPUT_DEVICES, 'w') as outfile:
        json.dump(devices, outfile, indent=2)

    with open(OUTPUT_ADJCIES, 'w') as outfile:
        json.dump(oscars_adjcies, outfile, indent=2)


def merge_up_downlink_adjcies(oscars_adjcies=None, uplink_adjcies=None, downlink_adjcies=None):
    adjcies = []
    if uplink_adjcies:
        adjcies.extend(uplink_adjcies)
    if downlink_adjcies:
        adjcies.extend(downlink_adjcies)
    for adjcy in adjcies:
        oscars_adjcies.append(adjcy)


def merge_isis_ports(devices=None, isis_ports=None):
    for device_name in isis_ports.keys():
        found_device = False
        for device in devices:
            if device["urn"] == device_name:
                found_device = True
                for port_name in isis_ports[device_name].keys():
                    mbps = isis_ports[device_name][port_name]["mbps"]
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


def merge_switches(devices=None, switches=None):
    for switch in switches:
        found = False
        for device in devices:
            if device["urn"] == switch["urn"]:
                found = True
        if found is True:
            error = "double inserting switch %s" % switch["urn"]
            raise ValueError(error)
        else:
            devices.append(switch)


def merge_edges(devices=None, edges=None):
    for device_name in edges.keys():
        found = False
        for device in devices:
            if device["urn"] == device_name:
                found = True
                for edge_info in edges[device_name]:
                    device["ifces"].append(edge_info)

        if found is False:
            raise ValueError("could not find device %s" % device_name)


def merge_all_ports(ports=None, devices=None):
    for device_name in ports.keys():
        found = False
        for device in devices:
            if device["urn"] == device_name:
                found = True
                for port in ports[device_name].keys():
                    port_data = ports[device_name][port]

                    if port_data["admin"] == "up":
                        ifce_data = {
                            "urn": device_name + ":" + port,
                            "reservableBw": port_data["mbps"]
                        }
                        if "is_isis" in port_data.keys():
                            ifce_data["capabilities"] = ["MPLS"]
                        else:
                            ifce_data["capabilities"] = ["ETHERNET"]

                        device["ifces"].append(ifce_data)
        if not found:
            raise ValueError("could not find")


def unpack_switches(packed_switches=None):
    unpacked_switches = []
    uplink_adjcies = []
    for switch in packed_switches.keys():
        switch_data = packed_switches[switch]
        unpacked_switch = {
            "urn": switch,
            "model": switch_data["model"],
            "type": "SWITCH",
            "capabilities": ["ETHERNET"],
            "ifces": []
        }
        for port_data in switch_data["edge"]:
            unpacked_switch["ifces"].extend(topo_util.expand_port(port_data=port_data, device=switch))

        if "reservableVlans" in switch_data:
            unpacked_switch["reservableVlans"] = switch_data["reservableVlans"]
        else:
            unpacked_switch["reservableVlans"] = topo_util.expand_vlans(switch_data["reservableVlansExpr"])

        for uplink in switch_data["uplink"]:
            urn = switch + ":" + uplink["port"]
            uplink_adjcy = {
                "a": urn,
                "z": uplink["remote"],
                "metrics": {"ETHERNET": 1}
            }
            uplink_adjcies.append(uplink_adjcy)
            unpacked_uplink_edge = {
                "urn": urn,
                "capabilities": ["ETHERNET"],
                "reservableBw": uplink["reservableBw"]
            }
            unpacked_switch["ifces"].append(unpacked_uplink_edge)
        unpacked_switches.append(unpacked_switch)

    return unpacked_switches, uplink_adjcies


def unpack_edges(packed_edges=None):
    unpacked_edges = {}
    downlink_adjcies = []

    for router in packed_edges.keys():
        router_edges = []
        if "edge" in packed_edges[router].keys():
            for edge_data in packed_edges[router]["edge"]:
                router_edges.extend(topo_util.expand_port(port_data=edge_data, device=router))
        unpacked_edges[router] = router_edges
        if "downlink" in packed_edges[router].keys():
            for downlink_data in packed_edges[router]["downlink"]:
                reservable_vlans = None
                if "reservableVlansExpr" in downlink_data.keys():
                    reservable_vlans = topo_util.expand_vlans(downlink_data["reservableVlansExpr"])
                elif "reservableVlans" in downlink_data.keys():
                    reservable_vlans = downlink_data["reservableVlans"]

                urn = router + ":" + downlink_data["port"]
                downlink_adjcy = {
                    "a": urn,
                    "z": downlink_data["remote"],
                    "metrics": {"ETHERNET": 1}
                }
                downlink_adjcies.append(downlink_adjcy)

                unpacked_downlink_edge = {
                    "urn": urn,
                    "capabilities": ["ETHERNET"],
                    "reservableBw": downlink_data["reservableBw"]
                }
                if reservable_vlans:
                    unpacked_downlink_edge["reservableVlans"] = reservable_vlans
                unpacked_edges[router].append(unpacked_downlink_edge)

    return unpacked_edges, downlink_adjcies


def verify_ports(ports=None):
    for router in ports.keys():
        for port in ports[router].keys():
            is_isis = ports[router][port]["is_isis"]
            is_customer = ports[router][port]["is_customer"]
            assert is_customer != is_isis


def today_ports_from_ipv4nets(ipv4nets=None, ports=None):
    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]
            the_type = ipv4net_info["type"]
            if the_type == "softwareLoopback" or the_type == "propVirtual":
                continue

            admin = ipv4net_info["admin"]
            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]

            is_isis = False
            is_customer = False
            ip_addr = ipv4net_info["ip_addr"]
            if len(ip_addr.keys()) == 1:
                address = ip_addr.keys()[0]
                address_info = ip_addr[address]
                if "isis_cost" in address_info.keys():
                    is_isis = True
                else:
                    is_customer = True

            if "port" in ipv4net_info.keys():
                port = ipv4net_info["port"]
            else:
                port = port_name(int_name)
            if router not in ports:
                ports[router] = {}
            if port not in ports[router]:
                ports[router][port] = {
                    "admin": admin,
                    "mbps": mbps,
                    "is_isis": is_isis,
                    "is_customer": is_customer,
                    "vlans": []
                }

            if is_isis:
                ports[router][port]["is_isis"] = True
            if is_customer:
                ports[router][port]["is_customer"] = True

            if "VLAN" in ipv4net_info.keys():
                vlan_id = int(ipv4net_info["VLAN"])
                ports[router][port]["vlans"].append(vlan_id)
    return ports


def today_ports_from_vlans(vlans=None):
    interfaces = []
    ports = {}

    for vlan_id in vlans.keys():
        grouped_by_vlan_id = vlans.get(vlan_id)
        vlan_id = int(vlan_id)
        for router in grouped_by_vlan_id.keys():
            for snmp_idx in grouped_by_vlan_id[router].keys():
                vlan_deets = grouped_by_vlan_id[router][snmp_idx]
                vlan_id_again = int(vlan_deets["VLAN"])
                assert vlan_id_again == vlan_id
                int_name = vlan_deets["int_name"]
                admin = vlan_deets["admin"]
                assert admin == "up" or admin == "down"
                mbps = vlan_deets["high_speed"]
                port = port_name(int_name)
                interface = {
                    "router": router,
                    "int_name": int_name,
                    "admin": admin,
                    "mbps": int(mbps),
                    "vlan_id": vlan_id
                }
                if router not in ports:
                    ports[router] = {}
                if port not in ports[router]:
                    ports[router][port] = {
                        "admin": admin,
                        "mbps": mbps,
                        "vlans": []
                    }

                ports[router][port]["vlans"].append(vlan_id)

                interfaces.append(interface)
    return ports, interfaces


def today_routers(router_system=None):
    out_routers = []
    for rs in router_system.values():
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
        model = "JUNOS_" + str(parts[0]).upper()
        return model
    else:
        raise ValueError("could not decide router model for [%s] [%s]" % (os, description))


def latency_of(addr=None, latency_db=None):
    if addr in latency_db:
        return latency_db[addr]["latency"]
    return None


def transform_adjcies(isis_adcjies=None):
    oscars_adjcies = []
    isis_ports = {}
    for isis_adjcy in isis_adcjies:
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

        if router_a not in isis_ports.keys():
            isis_ports[router_a] = {}

        isis_ports[router_a][port_a] = {
            "mbps": isis_adjcy["mbps"]
        }
    return oscars_adjcies, isis_ports


def make_isis_graph(isis=None):
    nodes = []
    edges = []
    for addr in isis.keys():
        entry = isis[addr]
        neighbor = entry["isis_neighbor"]
        if neighbor in isis:
            neighbor_entry = isis[neighbor]
            check_isis_neighborship(entry, neighbor_entry)
            edge = {
                "a": entry["router"],
                "z": neighbor_entry["router"],
                "mbps": entry["mbps"],
                "name": entry["int_name"] + " - " + neighbor_entry["int_name"],
                "a_port": entry["int_name"],
                "z_port": neighbor_entry["int_name"],
                "a_addr": addr,
                "z_addr": neighbor,
                "isis_cost": entry["isis_cost"],
                "latency": entry["latency"]
            }
            edges.append(edge)
            if entry["router"] not in nodes:
                nodes.append(entry["router"])
    return nodes, edges


def check_isis_neighborship(entry_a, entry_b):
    assert entry_a["isis_neighbor"] == entry_b["address"], "%s %s " % (entry_a["isis_neighbor"], entry_b["address"])
    assert entry_b["isis_neighbor"] == entry_a["address"], "%s %s " % (entry_b["isis_neighbor"], entry_a["address"])


def get_isis_neighbors(ipv4nets=None, latency_db=None):
    isis = {}

    for net in ipv4nets.keys():
        ipv4net = ipv4nets[net]
        for router in ipv4net.keys():
            ipv4net_info = ipv4net[router]
            ip_addr = ipv4net_info["ip_addr"]
            admin = ipv4net_info["admin"]
            if not admin or admin != "up":
                continue

            mbps = ipv4net_info["high_speed"]
            int_name = ipv4net_info["int_name"]

            if len(ip_addr.keys()) == 1:
                address = ip_addr.keys()[0]
                address_info = ip_addr[address]
                mask = address_info["mask"]

                if "isis_cost" in address_info.keys():
                    isis_cost = address_info["isis_cost"]
                    isis_status = address_info["isis_status"]
                    if isis_status:
                        isis_neighbor = address_info["isis_neighbor"]
                        latency = latency_of(addr=address, latency_db=latency_db)
                        if latency:
                            isis_entry = {
                                "address": address,
                                "router": router,
                                "latency": latency,
                                "mask": mask,
                                "int_name": int_name,
                                "mbps": mbps,
                                "isis_neighbor": isis_neighbor,
                                "isis_cost": isis_cost
                            }
                            isis[address] = isis_entry

    return isis


def port_name(int_name=None):
    parts = str(int_name).split(".")
    return parts[0]


if __name__ == '__main__':
    main()
