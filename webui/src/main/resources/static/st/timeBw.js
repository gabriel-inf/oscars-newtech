var bwViz;          // Bandwidth-Availability HTML container
var bwAvailMap;     // Bandwidth-Availability Bar Graph element
var bwData;         // Data in the map

var startTime;
var endTime;

var whichPicker = 0;
var currWindow;

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
    var newFurthest = newNow + 1000 * 60 * 60 * 24 * 365 * 2  // 2 years in the future
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

$(document).ready(function ()
{
    initializeBandwidthMap();

    $('#bwSlider').on('change', updateBandwidth)
});