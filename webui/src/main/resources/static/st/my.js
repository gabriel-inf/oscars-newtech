var selected_node_ids = {};

var display_viz = {};
var reservation_viz = {};

var add_node_to_resv_btn;

var resv_edge_params_form;
var resv_node_params_form;

var resv_viz_name = "reservation_viz";

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

function add_to_reservation(viz, name) {

    var last_added_node = null;
    var ds = viz.datasource;
    var prev_len = ds.nodes.length;
    console.log("existing nodes num: " + prev_len);

    if (prev_len > 0) {
        last_added_node = ds.nodes.get()[prev_len - 1];
        console.log("last added: " + last_added_node.id);
    }

    for (var i = 0; i < selected_node_ids.name.length; i++) {
        var nodeId = selected_node_ids.name[i];
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

function make_network(json_data, container, options, name) {

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

    attach_handlers(network, datasource, name);
    return result;
}

function attach_handlers(vis_js_network, vis_js_datasets, name) {

    vis_js_network.on('dragEnd', function (params) {
        selected_node_ids.name = [];
        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];
            console.log("dragEnd" + nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                console.log("dragEnd: cluster " + nodeId);
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
                selected_node_ids.name.push(nodeId);

            } else {
                console.log("dragEnd: plain " + nodeId);
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: true, y: true}});
                selected_node_ids.name.push(nodeId);
            }
        }
    });

    vis_js_network.on('dragStart', function (params) {
        var draggedPlain = false;
        var draggedCluster = false;
        selected_node_ids.name = [];

        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];
            if (vis_js_network.isCluster(nodeId) == true) {
                console.log("dragStart: cluster " + nodeId);
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});
                selected_node_ids.name.push(nodeId);
                var draggedCluster = true;

            } else {
                console.log("dragStart: plain " + nodeId);
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: false, y: false}});
                selected_node_ids.name.push(nodeId);
                draggedPlain = true;
            }
        }

        var is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }
        handle_click_(is_resv, false, true, draggedPlain);

    });

    vis_js_network.on("click", function (params) {
        var clickedNode = false;
        var clickedEdge = false;
        var clickedPlain = false;
        selected_node_ids.name = [];
        var i;

        for (i = 0; i < params.nodes.length; i++) {
            clickedNode = true;
            var nodeId = params.nodes[i];
            console.log("node selected " + nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                selected_node_ids.name.push(nodeId);
                console.log("cluster node selected " + nodeId);
            } else {
                clickedPlain = true;
                selected_node_ids.name.push(nodeId);
                console.log("plain node selected " + nodeId);
            }
        }

        var is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }

        if (!clickedNode) {
            for (i = 0; i < params.edges.length; i++) {
                clickedEdge = true;
                var edgeId = params.edges[i];
                edgeId = vis_js_network.clustering.getBaseEdge(edgeId);
                console.log("edge selected: " + edgeId);
            }
        }
        handle_click_(is_resv, clickedEdge, clickedNode, clickedPlain);

    });
}

function handle_click_(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain) {
    if (is_resv) {
        add_node_to_resv_btn.addClass("disabled").removeClass("active");

        if (selected_an_edge) {
            resv_edge_params_form.show();
        } else {
            resv_edge_params_form.hide();
        }
        if (selected_a_node) {
            show_resv_node_card(selected_node_ids.name[0]);
        } else {
            resv_node_params_form.hide();
        }
    } else {
        if (is_selected_node_plain) {
            add_node_to_resv_btn.removeClass("disabled").addClass("active");
        } else {
            add_node_to_resv_btn.addClass("disabled").removeClass("active");
        }
    }
}

function show_resv_node_card(nodeId) {
    console.log("showing card for " + nodeId);
    $('#resv_node_table tbody').empty();
    resv_node_params_form.show();
    var url = "/info/device/" + nodeId + "/vlanEdges";
    loadJSON(url, function (response) {
        var vlanEdges = JSON.parse(response);
        console.log(vlanEdges);
        vlanEdges.forEach(function (value, index, vlanEdges) {
            console.log("adding a row for " + value);
            var tr = "<tr>" +
                "<td>" + index + "</td>" +
                "<td>" + value + "</td>" +
                "<td><input name='bw" + index + "' type='text' placeholder='1G' class='form-control input-md'  /></td>" +
                "<td><input name='vlan" + index + "' type='text' placeholder='vlan' class='form-control input-md'  /></td>" +
                "<td><input name='use" + index + "' type='checkbox' class='form-control input-md'  /></td>" +
                "</tr>";
            $('#resv_node_table tbody').append(tr);
        })
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
                hover: false,
                navigationButtons: true,
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
        display_viz = make_network(json_data, nv_cont, nv_opts, "network_viz");

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
                addNode: false,
                addEdge: function (data, callback) {
                    if (data.from != data.to) {
                        callback(data);
                    }
                }
            }
        };

        $('#dump_positions_btn').on('click', function (e) {
            e.preventDefault();
            var pos = display_viz.network.getPositions();
            var jsonText = JSON.stringify(pos, null);
            $('#positions_display').text(jsonText);
            return false;

        });


        reservation_viz = make_network({}, rv_cont, rv_opts, "reservation_viz");

        resv_edge_params_form = $('#resv_edge_params_form');
        resv_edge_params_form.hide();

        resv_node_params_form = $('#resv_node_params_form');
        resv_node_params_form.hide();

        add_node_to_resv_btn = $('#add_node_to_resv_btn');

        add_node_to_resv_btn.on('click', function (e) {
            e.preventDefault();
            add_to_reservation(reservation_viz, resv_viz_name);
        });


    });

});

