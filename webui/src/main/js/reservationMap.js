const React = require('react');
import * as vis from "vis";

let netViz;         // Network map HTML container
let networkMap;     // Network map element
let netData;        // Data in the map
let netOptions;

let netPorts = [];      // All network ports
let vizLinks = [];      // All network links
let portCaps;           // Map of port bandwidth capacities

let filteredConnections = [];   // All (filtered) circuit reservations
let filteredConnectionIDs = [];   // All (filtered) circuit reservations - IDs only!
let filteredBandwidthValues = [];   // All (filtered) bandwidth reservations


class ReservationMap extends React.Component{

    render() {
        return (
            <div className="panel-group">
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4 className="panel-title">
                            <a data-toggle="collapse" href="#network_panel">Show / hide network</a>
                        </h4>
                    </div>
                    <div id="network_panel" className="panel-body collapse in">
                        <div id="loadingBarDiv" className="loadingBarDiv">
                            <div className="loadingBarBorder">
                                <div id="progressVal" className="loadingBarProgress">0%</div>
                                <div className="loadingBarRunner">
                                    <div id="progressBar" className="loadingBar" />
                                </div>
                            </div>
                            <div className="loadingBarMessage">Loading Network Topology...</div>
                        </div>
                        <div id="networkVisualization"/>
                    </div>
                </div>
            </div>
        );
    }
}

module.exports = ReservationMap;

function initializeNetwork()
{
    netViz = document.getElementById('networkVisualization');
    netPorts = [];
    vizLinks = [];

    // Identify the network ports
    loadJSON("/viz/listPorts", function (response)
    {
        let netPorts = JSON.parse(response);

        let stringifiedInput = JSON.stringify(netPorts);

        $.ajax({
            type: "POST",
            url: "/topology/bwcapacity",
            data: stringifiedInput,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response)
            {
                portCaps = response;
            }
        });
    });

    loadJSON("/viz/topology/multilayer", function (response)
    {
        let json_data = JSON.parse(response);
        let allLinks = json_data["edges"];

        for(let e = 0; e < allLinks.length; e++)
        {
            let edge = allLinks[e];

            if(edge.from !== null && edge.to !== null)
                vizLinks.push(edge);
        }

        netOptions = {
            autoResize: true,
            width: '100%',
            height: '400px',
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: false,
                dragView: false,
                multiselect: false,
                selectable: false,
            },
            physics: {
                stabilization: true,
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"},
            }
        };

        // create an array with nodes
        let nodes = new vis.DataSet(json_data['nodes']);
        let edges = new vis.DataSet(json_data['edges']);

        // create a network
        netData = {
            nodes: nodes,
            edges: edges,
        };

        networkMap = new vis.Network(netViz, netData, netOptions);

        // Listener for when network is loading and stabilizing
        networkMap.on("stabilizationProgress", function(params) {
            let maxWidth = 100;
            let minWidth = 0;
            let widthFactor = params.iterations/params.total;
            let width = Math.max(minWidth,maxWidth * widthFactor);

            document.getElementById('progressBar').style.width = width + '%';
            document.getElementById('progressVal').innerHTML = Math.round(widthFactor*100) + '%';
        });

        networkMap.once("stabilizationIterationsDone", function() {
            document.getElementById('progressVal').innerHTML = '100%';
            document.getElementById('progressBar').style.width = '496px';
            document.getElementById('loadingBarDiv').style.opacity = 0;

            // really clean the dom element
            setTimeout(function () {document.getElementById('loadingBarDiv').style.display = 'none';}, 500);
        });

        initializeConnectionList();     // Wait for topology to draw before displaying connections
    });
}

function updateResvPortBW(resvBwMap)
{
    // 1. Filter Bandwidth-consumption items to correspond only to those reservations currently in the list filter //
    let filteredBandwidthValues = resvBwMap.filter(function (bwItem){ return ($.inArray(bwItem.containerConnectionId, filteredConnectionIDs) !== -1) });

    //TODO: Filter Bandwidth consumption by display-time set by user

    // 2. Sum up bw at each port //
    let portConsumptionMap = new Map();
    let filteredPorts = [];

    for(let bw = 0; bw < filteredBandwidthValues.length; bw++)
    {
        let oneBwItem = filteredBandwidthValues[bw];
        let maxBW = Math.max(oneBwItem.inBandwidth, oneBwItem.egBandwidth);

        if(!portConsumptionMap.has(oneBwItem.urn))    // New port URN
        {
            portConsumptionMap.set(oneBwItem.urn, maxBW);
            filteredPorts.push(oneBwItem.urn);
        }
        else
        {
            let totalPortBW = portConsumptionMap.get(oneBwItem.urn);
            totalPortBW += maxBW;
            portConsumptionMap.set(oneBwItem.urn, totalPortBW);
        }
    }


    // 3. Map ports to links //
    let linkConsumptionMap = new Map();
    let filteredLinks = [];

    for(let p = 0; p < filteredPorts.length; p++)
    {
        let portID = filteredPorts[p];
        let matchingLinks = vizLinks.filter(function (link){ return (link["id"].indexOf(portID) !== -1)});   // Will contain at most one link

        if(matchingLinks.length === 0)      // Edge-ports not drawn on map. Ignore them
        {
            //console.log("No Link match for Port: " + portID);
        }
        else        // Need to filter out links included if portID is a substring. Example: [PortXYZ] could return links [PortXYZ -- PortABC] and [PortXYZ2 -- PortDEF]
        {
            for(let l = 0; l < matchingLinks.length; l++)
            {
                let linkPorts = matchingLinks[l]["id"].split(" -- ");
                if(linkPorts[0] === portID || linkPorts[1] === portID)
                {
                    linkConsumptionMap.set(matchingLinks[l].id, 0);
                    filteredLinks.push(matchingLinks[l].id);
                    //console.log("Port: " + portID + ", Link: " + matchingLinks[l].id);
                }
            }
        }
    }

    // 4. Compare bwConsumption at both ends of link and find maximum //
    for(let lk = 0; lk < filteredLinks.length; lk++)
    {
        let thisLink = filteredLinks[lk];
        let thisLinkParse = thisLink.split(" -- ");
        let aPortName = thisLinkParse[0];
        let zPortName = thisLinkParse[1];

        let aPortBW = -1;
        let zPortBW = -1;

        if(portConsumptionMap.has(aPortName) && portConsumptionMap.has(zPortName))  // Both ports have consumed bandwidth
        {
            aPortBW = portConsumptionMap.get(aPortName);
            zPortBW = portConsumptionMap.get(zPortName);
            let maxBW = Math.max(aPortBW, zPortBW);

            linkConsumptionMap.set(thisLink, maxBW);
        }
        else if(portConsumptionMap.has(aPortName))      // Only one port has consumed bandwidth
        {
            aPortBW = portConsumptionMap.get(aPortName);
            linkConsumptionMap.set(thisLink, aPortBW);
        }
        else if(portConsumptionMap.has(zPortName))
        {
            zPortBW = portConsumptionMap.get(zPortName);
            linkConsumptionMap.set(thisLink, zPortBW);
        }
        else
        {
            console.error("Error mapping bandwidth consumption to links");
        }
    }

    let allLinkDetails = new Map();

    for(let l = 0; l < vizLinks.length; l++)
    {
        let linkID = vizLinks[l].id;
        let linkBW = 0;

        if(linkConsumptionMap.has(linkID))
            linkBW = linkConsumptionMap.get(linkID);

        let linkCap = calculateLinkCapacity(linkID); // 5. Get link utilization as percentage of capacity
        let linkUtil = linkBW / linkCap;
        let linkColor = pickColor(linkUtil);      // 6. Select link color based on utilization

        allLinkDetails.set(linkID, {
            id: linkID,
            consumed: linkBW,
            capacity: linkCap,
            utilization: linkUtil,
            color: linkColor,
        });
    }

    // 7. Update and color links
    updateTopologyLinks(allLinkDetails);
}

function updateTopologyLinks(linkMap)
{
    for(let e = 0; e < vizLinks.length; e++)
    {
        let oneEdge = vizLinks[e];
        let linkDeets = linkMap.get(oneEdge.id);
        let linkCap = linkDeets.capacity;
        let linkBw = linkDeets.consumed;
        let units = " Mb/s";

        if(linkCap >= 1000)
        {
            linkCap = linkCap / 1000;
            units = " Gb/s";
        }

        let linkCapString = ", Capacity: " + linkCap + units;

        units = " Mb/s";

        if(linkBw >= 1000)
        {
            linkBw = linkBw / 1000;
            units = " Gb/s";
        }

        let linkBwString = ", Consumed: " + linkBw + units;

        let newTitle = oneEdge.id + linkCapString + linkBwString;
        let newColor = linkDeets.color;

        netData.edges.update([{id: oneEdge.id, title: newTitle, color: newColor}]);
    }
}

function pickColor(utilization)
{
    if(utilization === 0.0)
        return "#2F7FED";
    else
    {
        let redMax = 255;
        let goldVal = 215;
        let greenVal = goldVal - (utilization * goldVal);

        return "rgb(" + redMax + "," + Math.floor(greenVal) + ",0)";
    }
}


function calculateLinkCapacity(linkID)
{
    let linkSplit = linkID.split(" -- ");
    let aPortName = linkSplit[0];
    let zPortName = linkSplit[1];

    let aPortCap = portCaps[aPortName];
    let zPortCap = portCaps[zPortName];

    return Math.min(aPortCap, zPortCap);
}
