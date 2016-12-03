var bwViz;          // Bandwidth-Availability HTML container
var bwAvailMap;     // Bandwidth-Availability Bar Graph element
var bwData;         // Data in the map

var netViz;         // Network map HTML container
var networkMap;     // Network map element
var netData;        // Data in the map

var startTime;
var endTime;

var whichPicker = 0;
var currWindow;

var netPorts = [];      // All network ports
var vizLinks = [];      // All network links

var selectedERO = [];       // Updated ERO (nodes only) selected by user
var selectedLinks = [];     // Updated ERO (links only) selected by user
var adjacentLinks = [];

var button_clearERO;


function initializeNetwork()
{
    netViz = document.getElementById('networkVisualization');
    netPorts = [];
    vizLinks = [];

    // Identify the network ports
    loadJSON("/viz/listPorts", function (response)
    {
        var ports = JSON.parse(response);
        netPorts = ports;
    });

    loadJSON("/viz/topology/multilayer", function (response)
    {
        var json_data = JSON.parse(response);
        var allLinks = json_data["edges"];

        for(var e = 0; e < allLinks.length; e++)
        {
            var edge = allLinks[e];

            if(edge.from !== null && edge.to !== null)
                vizLinks.push(edge);
        }

        var netOptions = {
            autoResize: true,
            width: '90%',
            height: '400px',
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: false,
                dragView: true,
                multiselect: false,
                selectable: true,
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
        var nodes = new vis.DataSet(json_data['nodes']);
        var edges = new vis.DataSet(json_data['edges']);

        // create a network
        netData = {
            nodes: nodes,
            edges: edges,
        };

        networkMap = new vis.Network(netViz, netData, netOptions);

        // Listener for click events
        networkMap.on('click', function (properties) { nodeSelection(properties); });

        // Listener for when network is loading and stabilizing
        networkMap.on("stabilizationProgress", function(params) {
            var maxWidth = 100;
            var minWidth = 0;
            var widthFactor = params.iterations/params.total;
            var width = Math.max(minWidth,maxWidth * widthFactor);

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
    });
}

// Behavior of clicking the "Clear Path" button
function clearERO()
{
    networkMap.unselectAll();
    highlight_devices(netData, selectedERO, false, '');
    highlight_links(netData, selectedLinks, false, '');
    selectedERO = [];
    selectedLinks = [];
    adjacentLinks = [];

    resetBandwidthAvailabilityMap();

    button_clearERO.removeClass("active").addClass("disabled");
}

// This function builds and updates the selected ERO //
function nodeSelection(properties)
{
    if(properties.nodes.length === 0)       // Only consider node clicks
        return;

    var theNode = properties.nodes[0];
    var theAdjLinks = vizLinks.filter(function (link){ return ($.inArray(link.id, properties.edges) !== -1) });

    var index = $.inArray(theNode, selectedERO);

    var connectingLink;

    var removedERO = [];
    var removedLinks = [];

    // First node in the ERO
    if($.isEmptyObject(selectedERO))
    {
        selectedERO.push(theNode);
        // Update links for next node
        adjacentLinks = [];
        adjacentLinks = theAdjLinks.slice();
    }
    // Subsequent nodes in the ERO
    else if(index === -1)
    {
        var isAdjacentNode = false;
        // Only add to ERO if the node is adjacent to previous
        for(var e = 0; e < adjacentLinks.length; e++)
        {
            var oneLink = adjacentLinks[e];

            if(oneLink.from === theNode || oneLink.to === theNode)
            {
                isAdjacentNode = true;
                connectingLink = oneLink;
                break;
            }
        }

        if(isAdjacentNode)
        {
            selectedERO.push(theNode);
            selectedLinks.push(connectingLink.id);
            // Update adjacent links for next node
            adjacentLinks = [];
            adjacentLinks = theAdjLinks.slice();
        }
        else
        {
            alert("Selected node must be directly connected to previous node!");
        }
    }
    else if(index === 0)
    {
        removedERO = selectedERO.slice();
        removedLinks = selectedLinks.slice();
        selectedERO = [];
        selectedLinks = [];
        adjacentLinks = [];
    }
    else
    {
        adjacentLinks = [];
        var updatedERO = [];
        var updatedLinks = [];

        for(var i = 0; i < index; i++)
        {
            updatedERO.push(selectedERO[i]);
        }

        for(var i = index; i < selectedERO.length; i++)
        {
            removedERO.push(selectedERO[i]);
        }

        var lastNode = selectedERO[index-1];
        var lastRemovedNode = selectedERO[index];

        selectedERO = [];
        selectedERO = updatedERO;

        // Update the adjacent links for last remaining element in the ERO
        if(!$.isEmptyObject(selectedERO))
        {
            theAdjLinks = networkMap.getConnectedEdges(lastNode);
            adjacentLinks = vizLinks.filter(function (link){ return ($.inArray(link.id, theAdjLinks) !== -1) });
        }

        for(var e = 1; e < selectedERO.length; e++)
        {
            var srcNode = selectedERO[e-1];
            var dstNode = selectedERO[e];

            var linkToKeep = vizLinks.filter(function (link){ return ((link.from === srcNode && link.to === dstNode) || (link.to === srcNode && link.from === dstNode))});
            updatedLinks.push(linkToKeep[0].id);
        }

        selectedLinks = [];
        selectedLinks = updatedLinks;

        for(var r = 1; r < removedERO.length; r++)
        {
            var srcNode = removedERO[r-1];
            var dstNode = removedERO[r];

            var linkToRemove = vizLinks.filter(function (link){ return ((link.from === srcNode && link.to === dstNode) || (link.to === srcNode && link.from === dstNode))});
            removedLinks.push(linkToRemove[0].id);
        }

        var linkToRemove = vizLinks.filter(function (link){ return ((link.from === lastNode && link.to === lastRemovedNode) || (link.to === lastNode && link.from === lastRemovedNode))});
        removedLinks.push(linkToRemove[0].id);
    }

    networkMap.unselectAll();
    highlight_devices(netData, removedERO, false, '');
    highlight_devices(netData, selectedERO, true, 'green');
    highlight_links(netData, removedLinks, false, '');
    highlight_links(netData, selectedLinks, true, 'green');

    if(!$.isEmptyObject(selectedERO))
        button_clearERO.removeClass("disabled").addClass("active");
    else
        button_clearERO.removeClass("active").addClass("disabled");
}

function initializeBandwidthMap()
{
    bwViz = document.getElementById('bwVisualization');

    var nowDate = Date.now();
    var furthestDate = nowDate + 1000 * 60 * 60 * 24 * 365 * 2  // 2 years in the future

    var bwOptions = {
        style:'line',
        drawPoints: false,
        dataAxis: { icons:true },
        orientation:'bottom',
        start: new Date(Date.now() + 5000 * 60),
        end: new Date(Date.now() + 1000 * 60 * 60 * 24),
        zoomable: true,
        zoomMin: 1000 * 60 * 60,
        zoomMax: 1000 * 60 * 60 * 24 * 365 * 2,
        min: nowDate,
        max: furthestDate,
        shaded: {enabled: true, style: 'fill-opacity:0.5;',},
        width: '90%',
        height: '400px',
        minHeight: '400px',
        maxHeight: '400px',
        legend: {enabled: false, icons: false},
        interpolation: {enabled: false},
        dataAxis: {left: {range: {min: 0, max: 30},},},
    }

    bwData = new vis.DataSet();

    bwData.add({x: nowDate + 1000 * 60 * 60, y: 25, group: 1});
    bwData.add({x: nowDate + 7000 * 60 * 60, y: 10, group: 1});
    bwData.add({x: nowDate + 7000 * 60 * 60, y: 25, group: 1});
    bwData.add({x: nowDate + 15000 * 60 * 60, y: 15, group: 1});
    bwData.add({x: nowDate + 15000 * 60 * 60, y: 10, group: 1});
    bwData.add({x: nowDate + 18000 * 60 * 60, y: 15, group: 1});

    // Create the Bar Graph
    bwAvailMap = new vis.Graph2d(bwViz, bwData, bwOptions);

    // Set first time bar: Start Time
    startTime =  nowDate + 50000 * 60;
    var startBarID = "starttime";
    bwAvailMap.addCustomTime(startTime, startBarID);

    // Set second time bar: End Time
    endTime =  nowDate + 900 * 60 * 60 * 24;
    var endBarID = "endtime";
    bwAvailMap.addCustomTime(endTime, endBarID);

    // Initialize date info for start/end
    document.getElementById('start-time').innerHTML = new Date(startTime);
    document.getElementById('end-time').innerHTML = new Date(endTime);

    currWindow = bwAvailMap.getWindow();

    // Listener for changing start/end times
    bwAvailMap.on('timechange', function (properties) { changeTime(properties, startBarID, endBarID); });

    // Listener for double-click event
    bwAvailMap.on('doubleClick', function (properties) { moveDatePicker(properties, startBarID, endBarID); });

    // Listener for range-change event
    bwAvailMap.on('rangechange', function (properties) { currWindow = bwAvailMap.getWindow(); });

    // Listener to redraw map as time progresses
    bwAvailMap.on('currentTimeTick', function (properties) { refreshMap(); });

    // Listener for changing bandwidth values
    $("#bwSlider").on("input change", function() { updateBandwidth(); });

    updateBandwidth();      // Initial b/w value
}

// This function controls what happens when the user clicks and drags one of the reservation window start/end-time bars
function changeTime(properties, startBarID, endBarID)
{
    var barID = properties.id;
    var startBarTime;
    var endBarTime;

    if(barID === startBarID)
    {
        startBarTime = properties.time;
        endBarTime = bwAvailMap.getCustomTime(endBarID);

        if(startBarTime <= endBarTime)      // Simple case, Start earlier than End
        {
            startTime = startBarTime;
            document.getElementById('start-time').innerHTML = startTime;
        }
        else                                // Complex case, User has dragged Start to a point later than End
        {
            startTime = endBarTime;
            endTime = startBarTime;
            document.getElementById('start-time').innerHTML = startTime;
            document.getElementById('end-time').innerHTML = endTime;
        }
    }
    else
    {
        endBarTime = properties.time;
        startBarTime = bwAvailMap.getCustomTime(startBarID);

        if(startBarTime <= endBarTime)      // Simple case, End later than Start
        {
            endTime = endBarTime;
            document.getElementById('end-time').innerHTML = endBarTime;
        }
        else                                // Complex case, User has dragged End to a point earlier than Start
        {
            startTime = endBarTime;
            endTime = startBarTime;
            document.getElementById('start-time').innerHTML = startTime;
            document.getElementById('end-time').innerHTML = endTime;
        }
    }

    updateBandwidth();
}

function updateBandwidth()
{
    var bwSlider = document.getElementById('bwSlider');
    document.getElementById('bandwidth').innerHTML = bwSlider.value;

    var isAvailable = inspectAvailability(bwSlider.value);

    var color = 'red';
    if(isAvailable)
    {
        color = 'green';
    }

    var linestyle = 'stroke-width:5;' + ' stroke:' + color + ';';
    var fillstyle = 'fill-opacity:0.7;' + ' fill:' + color + ';';

    var groupData = {
            id: "bwBar",
            content: "Group Name",
            style: linestyle,
            options: {
                shaded: {enabled: true, orientation: 'bottom', style: fillstyle,},
            }
    };

    var groups = new vis.DataSet();
    groups.add(groupData);
    bwAvailMap.setGroups(groups);

    var oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'bwBar'; }});
    var newBwValueLeft  = {x: startTime, y: bwSlider.value, group: 'bwBar'};
    var newBwValueRight = {x: endTime, y: bwSlider.value, group: 'bwBar'};

    bwData.remove(oldBwValues);
    bwData.add(newBwValueLeft);
    bwData.add(newBwValueRight);
}

// This function determines whether to draw the reservation selection red or green. Red if bandwidth availability cannot support it, green otherwise. //
function inspectAvailability(bandwidth)
{
    var isReservable = true;

    var allAvailabilityValues = bwData.getIds({ filter: function (item)
    {
        if(item.x >= startTime && item.x <= endTime && item.group !== 'bwBar')
            return item;
    }});


    for(var b = 0; b < allAvailabilityValues.length; b++)
    {
        var oneItem = bwData.get(allAvailabilityValues[b]);

        //console.log("ID: " + oneItem.id + ", X: " + oneItem.x + ", Y: " + oneItem.y);

        if(oneItem.y < bandwidth)   // Cannot reserve
        {
            isReservable = false;
            break;
        }
    }

    // Check to make sure reservation window is not returning a false positive by being between data points.
    if(isReservable)
    {
        var nearestItem = null;

        allAvailabilityValues = bwData.getIds({ filter: function (item)
        {
                if(item.x < startTime && item.group !== 'bwBar')
                    return item;
        }});

        for(var b = 0; b < allAvailabilityValues.length; b++)
        {
            var oneItem = bwData.get(allAvailabilityValues[b]);
            var timeDiff = startTime - oneItem.x;

            if(nearestItem === null && timeDiff > 0)
            {
                nearestItem = oneItem;
                continue;
            }

            if(nearestItem !== null)
            {
                if(timeDiff > 0 && timeDiff < (startTime - nearestItem.x))
                {
                    nearestItem = oneItem;
                }
            }
        }

        if(nearestItem !== null)
        {
            if(nearestItem.y < bandwidth)
                isReservable = false;
        }
    }

    return(isReservable);
}

// Controls behavior of double-clicking inside the BW Availability Map
function moveDatePicker(properties, startBarID, endBarID)
{
    if(whichPicker === 0)
    {
        bwAvailMap.setCustomTime(properties.time, startBarID);      // Doesn't automatically trigger the timechange event
        var changeProp = {"id": startBarID, "time": properties.time};
        changeTime(changeProp, startBarID, endBarID);               // Trigger it manually
        whichPicker = 1;
    }
    else
    {
        bwAvailMap.setCustomTime(properties.time, endBarID);        // Doesn't automatically trigger the timechange event
        var changeProp = {"id": endBarID, "time": properties.time};
        changeTime(changeProp, startBarID, endBarID);               // Trigger it manually
        whichPicker = 0;
    }
}

function refreshMap()
{
    var newNow = bwAvailMap.getCurrentTime();
    var newFurthest = newNow + 1000 * 60 * 60 * 24 * 365 * 2;  // 2 years in the future
    var newEnd = new Date();

    bwAvailMap.options.min = newNow;
    bwAvailMap.options.max = newFurthest;

    if(currWindow.start > bwAvailMap.getCurrentTime())      // Don't redraw if not all the way left, or the map will slide automatically ruining the experience
            return;

    var window = bwAvailMap.getWindow();
    var newDiff = window.end - window.start;

    var nowAsMillis = newNow.getTime();

    newEnd.setTime(nowAsMillis + newDiff);

    bwAvailMap.setWindow(newNow, newEnd);
    bwAvailMap.getWindow();

}

function getBandwidthAvailability()
{
    ; // To be implemented by Anand!!!
}

// Clears data from existing map - Triggered by clearERO() - May not be necessary
function resetBandwidthAvailabilityMap()
{
    ; // To be implemented by Anand!!!
}

function drawBandwidthAvailabilityMap()
{
    ; // To be implemented by Anand!!!
}

function submitRequestedReservation()
{
    ; // To be implemented by Anand!!!
}


$(document).ready(function ()
{
    initializeNetwork();
    initializeBandwidthMap();

    $('#bwSlider').on('change', updateBandwidth);

    button_clearERO = $('#buttonCancelERO');
});