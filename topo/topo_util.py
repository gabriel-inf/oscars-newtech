#!/usr/bin/env python
# encoding: utf-8


def filter_address_info(ipv4net_info=None):
    ip_addr = ipv4net_info["ip_addr"]

    if len(ip_addr.keys()) == 1:
        address = ip_addr.keys()[0]
        address_info = ip_addr[address]
        return address, address_info

    else:
        weird = True
        for address in ip_addr.keys():
            if address == "128.0.0.1":
                weird = False
            alias = str(ipv4net_info["alias"])
            if alias.find("stub"):
                weird = False

        if weird:
            pp.pprint(ipv4net_info)
            return None, None
        else:
            return None, None


def guess_port(ipv4net_info=None):
    if "port" in ipv4net_info.keys():
        return ipv4net_info["port"]

    int_name = str(ipv4net_info["int_name"])
    juniper_ifce_prefix = ["ge", "xe", "ae", "fe", "t3"]

    for prefix in juniper_ifce_prefix:
        if int_name.startswith(prefix):
            return int_name.split(".")[0]

    return None