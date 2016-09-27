#!/usr/bin/env python
# encoding: utf-8

import re


def expand_port(port_data=None, device=None):
    result = []
    port_names = []
    if "port" in port_data.keys():
        port_names.append(port_data["port"])
    elif "portExpr" in port_data.keys():
        regex = "\[([\d,-]+)\]"
        port_expr = str(port_data["portExpr"])
        parts = re.split(regex, port_expr)

        expanded_names = expand_expr_parts(parts=parts)
        port_names.extend(expanded_names)

    reservable_vlans = None
    if "reservableVlansExpr" in port_data.keys():
        reservable_vlans = expand_vlans(port_data["reservableVlansExpr"])
    elif "reservableVlans" in port_data.keys():
        reservable_vlans = port_data["reservableVlans"]

    for port in port_names:
        expanded_port = {
            "urn": device+":"+port,
            "capabilities": ["ETHERNET"],
            "reservableBw": port_data["reservableBw"]
        }
        if reservable_vlans:
            expanded_port["reservableVlans"] = reservable_vlans
        result.append(expanded_port)
    return result


def expand_vlans(vlans_expr=None):
    vlans = expand_numeric_range(vlans_expr)
    tuples = collapse_int_set(vlans)
    return tuples


def expand_expr_parts(parts=None):
    expanded = []
    if len(parts) == 1:
        expanded.append(parts[0])
        return expanded
    elif len(parts) % 2 == 0:
        raise ValueError("invalid number of expression parts")

    if len(parts) > 3:
        start = parts.pop(0)
        range_expr = parts.pop(0)
        sub_expanded = expand_expr_parts(parts=parts)
        for sub_exp in sub_expanded:
            partial = expand_range_expr(start=start, range_expr=range_expr, end=sub_exp)
            expanded.extend(partial)
    else:
        start = parts[0]
        range_expr = parts[1]
        end = parts[2]
        partial = expand_range_expr(start=start, range_expr=range_expr, end=end)
        expanded.extend(partial)

    return expanded


def expand_range_expr(start=None, range_expr=None, end=None):
    expanded = []
    numbers = expand_numeric_range(range_expr=range_expr)
    for num in numbers:
        out = "%s%s%s" % (start, num, end)
        expanded.append(out)
    return expanded


def collapse_int_set(integers=None):
    integers.sort()
    result = []
    floor = integers[0]
    ceiling = integers[0]
    for num in integers:
        if ceiling + 1 < num:
            the_tuple = {"floor": floor, "ceiling": ceiling}
            result.append(the_tuple)
            floor = num
        ceiling = num
    the_tuple = {"floor": floor, "ceiling": ceiling}
    result.append(the_tuple)
    return result


def expand_numeric_range(range_expr=None):
    numbers = []
    expr_parts = range_expr.split(",")
    for part in expr_parts:
        range_parts = part.split("-")
        if len(range_parts) > 1:
            floor = int(range_parts[0])
            ceil = int(range_parts[1])
            if ceil >= floor:
                while floor <= ceil:
                    numbers.append(floor)
                    floor += 1
            else:
                raise ValueError("bad number expression")
        else:
            numbers.append(int(part))
    return numbers
