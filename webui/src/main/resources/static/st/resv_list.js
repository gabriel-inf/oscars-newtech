var netViz;         // Network map HTML container
var networkMap;     // Network map element
var netData;        // Data in the map

var netPorts = [];      // All network ports
var vizLinks = [];      // All network links

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

    loadJSON("/viz/topology/unidirectional", function (response)
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
        var nodes = new vis.DataSet(json_data['nodes']);
        var edges = new vis.DataSet(json_data['edges']);

        // create a network
        netData = {
            nodes: nodes,
            edges: edges,
        };

        networkMap = new vis.Network(netViz, netData, netOptions);

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

// Retrieves and stores the full set of ReservedB
function getAllReservedBWs()
{
    console.log("Updated Links");
    setTimeout(getAllReservedBWs, 30000);   // Updates every 30 seconds
}

function showDetails(connectionToShow)
{
    var connID = connectionToShow.id.split("accordion_");

    drawReservation(connID[1]);
}

function clearDetails(connectionToShow)
{
    var connID = connectionToShow.id.split("accordion_");

    clearReservation(connID[1]);
}

var drawReservation = function(connectionID)
{
    var vizName = "resViz_" + connectionID;
    var emptyVizName = "emptyViz_" + connectionID;

    var vizElement = document.getElementById(vizName);
    var emptyVizElement = document.getElementById(emptyVizName);

    loadJSON("/viz/connection/" + connectionID, function (response)
    {
        var json_data = JSON.parse(response);
        console.log(json_data);

        edges = json_data.edges;
        nodes = json_data.nodes;

        if(edges.length === 0 || nodes.length === 0)
        {
            $(vizElement).hide();
            $(emptyVizElement).show();
            return;
        }
        else
        {
            $(vizElement).show();
            $(emptyVizElement).hide();
        }

        // Parse JSON string into object
        var nv_opts = {
            height: '400px',
            interaction: {
                hover: false,
                zoomView: true,
                dragView: true
            },
            physics: {
                stabilization: true
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"}
            }
        };
        reservation_viz = make_network(json_data, vizElement, nv_opts, vizName);
    });
};

var clearReservation = function(vizElement)
{
    //TODO: Figure out how to clear/destroy the reservation view so things don't get slow
    console.log("Need to figure out how to clear/destroy the reservation view so things don't get slow!");
}

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId)
{
    //TODO: Implement some actions when parts of reservation viz are selected
    ;
}

$(document).ready(function ()
{
    initializeNetwork();

    setTimeout(getAllReservedBWs, 5000);
    /*var allCollapsibles = document.getElementsByClassName('accordion-body collapse');

    for(var c = 0; c < allCollapsibles.length; c++)
    {
        var oneCollapsible = allCollapsibles[c];
        $(oneCollapsible).on('hide.bs.collapse', clearReservation(oneCollapsible)); // This doesn't trigger on click, only when loading page! :(
    }
    */
});


