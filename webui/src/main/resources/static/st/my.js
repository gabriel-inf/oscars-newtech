var selected_node_ids = [];
var display_viz = {};
var reservation_viz = {};
var add_form;

var resv_edge_params_form;
var resv_node_params_form;


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

function add_to_reservation(viz) {

    var last_added_node = null;
    var ds = viz.datasource;
    var prev_len = ds.nodes.length;
    console.log("existing nodes num: " + prev_len);

    if (prev_len > 0) {
        last_added_node = ds.nodes.get()[prev_len - 1];
        console.log("last added: " + last_added_node.id);
    }

    for (var i = 0; i < selected_node_ids.length; i++) {
        var nodeId = selected_node_ids[i];
        console.log("adding node " + nodeId);
        if (!ds.nodes.get(nodeId)) {
            ds.nodes.add({id: nodeId, label: nodeId});
            if (last_added_node != null) {
                var a = last_added_node.id;
                var z = nodeId;
                var newId = a + " -- " + z;
                var newEdge = {
                    id: newId,
                    from: a,
                    to: z
                };
                console.log("adding edge: " + newId);

                ds.edges.add(newEdge);

            }
        }
    }

    viz.network.stabilize();

}

function make_network(json_data, container, options, is_resv_form) {

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

    attach_handlers(network, datasource, is_resv_form);
    return result;
}

function attach_handlers(vis_js_network, vis_js_datasets, is_resv_form) {

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
        var clickedEdge = false;
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
                clickedEdge = true;
                var edgeId = params.edges[i];
                edgeId = vis_js_network.clustering.getBaseEdge(edgeId);
                console.log("edge selected: " + edgeId);
            }
        }

        if (is_resv_form) {
            if (clickedEdge) {
                resv_edge_params_form.show()
            } else {
                resv_edge_params_form.hide();
            }
            if (clickedNode) {
                resv_node_params_form.show()
            } else {
                resv_node_params_form.hide();
            }
        }

        if (clickedPlain) {
            add_form.show();
        } else {
            add_form.hide();
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
        display_viz = make_network(json_data, nv_cont, nv_opts, false);

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
            },
            manipulation: {
                addEdge: function (data, callback) {
                    if (data.from != data.to) {
                        callback(data);
                    }
                }
            }
        };

        reservation_viz = make_network({}, rv_cont, rv_opts, true);

        resv_edge_params_form = $('#resv_edge_params_form');
        resv_edge_params_form.hide();

        resv_node_params_form = $('#resv_node_params_form');
        resv_node_params_form.hide();


        add_form = $('#add_to_resv_form');
        add_form.hide();

        add_form.on('submit', function (e) {
            e.preventDefault();
            add_to_reservation(reservation_viz);
        });


    });

});

