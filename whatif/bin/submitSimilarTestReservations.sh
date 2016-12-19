#!/bin/bash

curl -H "Content-Type: application/json" -k --user oscars:oscars-shared -X POST -d '{"start": "12 20 2016 15:30", "end": "12 20 2016 16:30", "connectionId": "test1", "username": "Tester", "description": "What-If test", "flows": [{"sourcePorts": ["kans-cr5:10/1/1"], "sourceDevice": "kans-cr5", "destPorts": ["chic-cr5:10/1/1"], "destDevice": "chic-cr5", "azMbps": "1000", "zaMbps": "1000", "sourceVlan": "any", "destVlan": "any", "azRoute": [], "zaRoute": [], "blacklist": [], "palindromic": "PALINDROME", "survivability": "NONE", "numDisjointPaths": "1"}], "minNumFlows": "1", "maxNumFlows": "1"}' https://localhost:8009/whatif/resv_simple/connection/add_commit

echo ""

curl -H "Content-Type: application/json" -k --user oscars:oscars-shared -X POST -d '{"start": "12 20 2016 08:30", "end": "12 20 2016 16:00", "connectionId": "test2", "username": "Tester", "description": "What-If test", "flows": [{"sourcePorts": ["kans-cr5:10/1/1"], "sourceDevice": "kans-cr5", "destPorts": ["chic-cr5:10/1/1"], "destDevice": "chic-cr5", "azMbps": "4000", "zaMbps": "4000", "sourceVlan": "any", "destVlan": "any", "azRoute": [], "zaRoute": [], "blacklist": [], "palindromic": "PALINDROME", "survivability": "NONE", "numDisjointPaths": "1"}], "minNumFlows": "1", "maxNumFlows": "1"}' https://localhost:8009/whatif/resv_simple/connection/add_commit

echo ""

curl -H "Content-Type: application/json" -k --user oscars:oscars-shared -X POST -d '{"start": "12 20 2016 06:00", "end": "12 20 2016 08:00", "connectionId": "test3", "username": "Tester", "description": "What-If test", "flows": [{"sourcePorts": ["kans-cr5:10/1/1"], "sourceDevice": "kans-cr5", "destPorts": ["chic-cr5:10/1/1"], "destDevice": "chic-cr5", "azMbps": "8000", "zaMbps": "8000", "sourceVlan": "any", "destVlan": "any", "azRoute": [], "zaRoute": [], "blacklist": [], "palindromic": "PALINDROME", "survivability": "NONE", "numDisjointPaths": "1"}], "minNumFlows": "1", "maxNumFlows": "1"}' https://localhost:8009/whatif/resv_simple/connection/add_commit

echo ""

