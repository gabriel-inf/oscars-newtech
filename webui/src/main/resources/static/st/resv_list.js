
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
    /*var allCollapsibles = document.getElementsByClassName('accordion-body collapse');

    for(var c = 0; c < allCollapsibles.length; c++)
    {
        var oneCollapsible = allCollapsibles[c];
        $(oneCollapsible).on('hide.bs.collapse', clearReservation(oneCollapsible)); // This doesn't trigger on click, only when loading page! :(
    }
    */
});


