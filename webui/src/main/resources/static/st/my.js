var selected_node_ids = [];
var display_viz = {};
var reservation_viz = {};
var add_form;


function loadJSON(url, callback) {

    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType('application/json');
    xobj.open('GET', url, true);
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            if (xobj.status == '200') {
                callback(xobj.responseText);
            }
        }
    };
    xobj.send(null);
}

function add_to_reservation() {

    for (var i = 0; i < selected_node_ids.length; i++) {
        var nodeId = selected_node_ids[i];
        console.log("adding node "+nodeId);
        var nodes = reservation_viz.datasource.nodes;
        if (!nodes.get(nodeId)) {
            nodes.add({id: nodeId, label: nodeId});

        }
    }
}

function make_network(json_data, container, options) {

    // create an array with nodes
    var nodes = new vis.DataSet(json_data['nodes']);
    var edges = new vis.DataSet(json_data['edges']);

    // create a network
    var datasource = {
        nodes: nodes,
        edges: edges
    };

    var network = new vis.Network(container, datasource, options);

    var result = {};
    result.network = network;
    result.datasource = datasource;

    attach_handlers(network, datasource);
    return result;
}

function attach_handlers(vis_js_network, vis_js_datasets) {

    vis_js_network.on('dragEnd', function (params) {
        selected_node_ids = [];
        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];
            console.log("dragEnd" + nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                console.log("dragEnd: cluster " + nodeId);
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
                selected_node_ids.push(nodeId);

            } else {
                console.log("dragEnd: plain " + nodeId);
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: true, y: true}});
                selected_node_ids.push(nodeId);
            }
        }
    });

    vis_js_network.on('dragStart', function (params) {
        var draggedNode = false;
        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];
            console.log("dragStart " + nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                console.log("dragStart: cluster " + nodeId);
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});

            } else {
                console.log("dragStart: plain " + nodeId);
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: false, y: false}});
                draggedNode = true;
            }
        }
        if (draggedNode) {
            add_form.show();
        }
    });

    vis_js_network.on("click", function (params) {
        var clickedNode = false;
        var clickedPlain = false;
        selected_node_ids = [];
        var i;

        for (i = 0; i < params.nodes.length; i++) {
            clickedNode = true;
            var nodeId = params.nodes[i];
            console.log("node selected " + nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                selected_node_ids.push(nodeId);
                console.log("cluster node selected " + nodeId);
            } else {
                clickedPlain = true;
                selected_node_ids.push(nodeId);
                console.log("plain node selected " + nodeId);
            }
        }

        if (!clickedNode) {
            for (i = 0; i < params.edges.length; i++) {
                var edgeId = params.edges[i];
                edgeId = vis_js_network.clustering.getBaseEdge(edgeId);
                console.log("edge selected: " + edgeId);

            }
        }
        if (clickedPlain) {
            add_form.show();
        }
    });
}


$(document).ready(function () {

    loadJSON("/graphs/multilayer", function (response) {

        var json_data = JSON.parse(response);

        // Parse JSON string into object
        var nv_cont = document.getElementById('network_viz');
        var nv_opts = {
            height: '500px',
            interaction: {
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
        display_viz = make_network(json_data, nv_cont, nv_opts);

        var rv_cont = document.getElementById('reservation_viz');
        var rv_opts = {
            height: '300px',
            interaction: {
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

        reservation_viz = make_network({}, rv_cont, rv_opts);

        add_form = $('#add_to_resv_form');
        add_form.hide();

        add_form.on('submit', function (e) {
            e.preventDefault();
            add_to_reservation();
        });


    });

});

