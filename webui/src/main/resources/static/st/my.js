var selected_node_ids = {};

var display_viz = {};
var reservation_viz = {};

var add_junction_btn;

var pipe_card;
var junction_card;

var resv_viz_name = "reservation_viz";

var reservation_request = {
    "junctions": {},
    "pipes": {}
};

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

    if (prev_len > 0) {
        last_added_node = ds.nodes.get()[prev_len - 1];
    }

    var junctions = reservation_request["junctions"];

    for (var i = 0; i < selected_node_ids.name.length; i++) {
        var nodeId = selected_node_ids.name[i];
        if (!(nodeId in junctions)) {
            junctions[nodeId] = {"fixtures": {}};
        }
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
                reservation_request["pipes"][newId] = {"bw": 0, "a": a, "z": z};

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

            selected_node_ids.name.push(nodeId);
            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: true, y: true}});
            }
        }
    });

    vis_js_network.on('dragStart', function (params) {
        var draggedPlain = false;
        selected_node_ids.name = [];
        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];
            selected_node_ids.name.push(nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: false, y: false}});
                draggedPlain = true;
            }
        }

        var is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }
        trigger_form_changes(is_resv, false, true, draggedPlain);

    });

    vis_js_network.on("click", function (params) {
        var clickedNode = false;
        var clickedEdge = false;
        var clickedPlain = false;
        var edgeId = "";
        var nodeId = "";
        selected_node_ids.name = [];
        var i;

        for (i = 0; i < params.nodes.length; i++) {
            clickedNode = true;
            nodeId = params.nodes[i];

            if (!vis_js_network.isCluster(nodeId) == true) {
                clickedPlain = true;
            }
            selected_node_ids.name.push(nodeId);
        }

        var is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }

        if (!clickedNode) {
            for (i = 0; i < params.edges.length; i++) {
                clickedEdge = true;
                edgeId = params.edges[i];
                edgeId = vis_js_network.clustering.getBaseEdge(edgeId);
                console.log("edge selected: " + edgeId);
            }
        }
        trigger_form_changes(is_resv, clickedEdge, clickedNode, clickedPlain, nodeId, edgeId);
    });
}

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId) {
    if (is_resv) {
        add_junction_btn.addClass("disabled").removeClass("active");

        if (selected_an_edge) {
            show_pipe_card(edgeId);
        } else {
            pipe_card.hide();
        }
        if (selected_a_node) {
            show_junction_card(nodeId);
        } else {
            junction_card.hide();
        }
    } else {
        if (is_selected_node_plain) {
            add_junction_btn.removeClass("disabled").addClass("active");
        } else {
            add_junction_btn.addClass("disabled").removeClass("active");
        }
    }
}

function show_pipe_card(edgeId) {
    console.log("showing card for pipe " + edgeId);
    var pipes = reservation_request["pipes"];
    console.log(pipes);
    pipe_card.show();

    // populate
    if (!(edgeId in pipes)) {
        console.log("new pipe, setting bw to 0 for: " + edgeId);
        pipes[edgeId] = {"bw": 0};
    }
    var prev_bw = pipes[edgeId]["bw"];
    var prev_a = pipes[edgeId]["a"];
    var prev_z = pipes[edgeId]["z"];

    console.log("updating form for edge: " + edgeId + " to bw: " + prev_bw);
    // detach previous event handlers
    $("#pipe_bw").off("change");
    $("#pipe_bw").val(prev_bw);
    $("#pipe_a").val(prev_a);
    $("#pipe_z").val(prev_z);

    // add new event handler
    $("#pipe_bw").change(function () {
        var bw = $("#pipe_bw").val();
        console.log("saving bw for pipe: " + edgeId + " : " + bw);
        pipes[edgeId]["bw"] = bw;
        console.log(reservation_request);
    });
}

function show_junction_card(nodeId) {
    console.log("showing card for junction " + nodeId);
    $('#resv_node_table tbody').empty();
    junction_card.show();

    var url = "/info/device/" + nodeId + "/vlanEdges";
    loadJSON(url, function (response) {
        var current_fixtures = JSON.parse(response);
        current_fixtures.forEach(function (value, index, current_fixtures) {
            var tr = "<tr>" +
                "<td>" + index + "</td>" +
                "<td>" + value + "</td>" +
                "<td><div class='form-check'><label class='form-check-label'>" +
                "<input id='use_" + index + "' type='checkbox' placeholder='vlan' class='form-check-input' />" +
                "</label></div></td>" +
                "<td><input id='bw_" + index + "' type='text' value='0' class='form-control input-sm'  /></td>" +
                "<td><input id='vlan_" + index + "' type='text' value='2-4094' class='form-control input-sm'  /></td>" +
                "</tr>";
            $('#resv_node_table tbody').append(tr);

            populate_junction(nodeId, current_fixtures);

            $('#use_' + index).click(function () {
                update_junction(nodeId, current_fixtures);
            });
            $("#bw_" + index).change(function () {
                $('#use_' + index).prop('checked', true);
                update_junction(nodeId, current_fixtures);
            });
            $("#vlan_" + index).change(function () {
                $('#use_' + index).prop('checked', true);
                update_junction(nodeId, current_fixtures);
            });
        });

    });
}

function populate_junction(nodeId, fixtures) {
    var junctions = reservation_request["junctions"];
    if (!(nodeId in junctions)) {
        junctions[nodeId] = {"fixtures": {}};
        return;
    } else if (!("fixtures" in junctions[nodeId])) {
        junctions[nodeId] = {"fixtures": {}};
    }
    fixtures.forEach(function (urn, index, fixtures) {
        if (urn in junctions[nodeId]["fixtures"]) {
            var bw = junctions[nodeId]["fixtures"][urn]["bw"];
            var vlan = junctions[nodeId]["fixtures"][urn]["vlan"];
            $("#use_" + index).prop('checked', true);
            $("#vlan_" + index).val(vlan);
            $("#bw_" + index).val(bw);
        }
    });
}


function update_junction(nodeId, fixtures) {
    var i;
    var junctions = reservation_request["junctions"];
    junctions[nodeId] = {
        "fixtures": {}
    };

    for (i = 0; i < fixtures.length; i++) {
        if ($("#use_" + i).is(':checked')) {
            junctions[nodeId]["fixtures"][fixtures[i]] = {
                "bw": $("#bw_" + i).val(),
                "vlan": $("#vlan_" + i).val()
            }
        }
    }

    console.log(reservation_request);
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
                addEdge: function (edgeData, callback) {
                    if (edgeData.from != edgeData.to) {
                        callback(edgeData);

                        reservation_request["pipes"][edgeData.id] = {
                            "bw": 0,
                            "a": edgeData.from,
                            "z": edgeData.to
                        }
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

        pipe_card = $('#pipe_card');
        pipe_card.hide();

        $('#pipe_form').on('submit', function (e) {
            e.preventDefault();
        });

        junction_card = $('#junction_card');
        junction_card.hide();


        add_junction_btn = $('#add_junction_btn');

        add_junction_btn.on('click', function (e) {
            e.preventDefault();
            add_to_reservation(reservation_viz, resv_viz_name);
        });

        $(function () {
            var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
            $(document).ajaxSend(function (e, xhr, options) {
                xhr.setRequestHeader(header, token);
            });
        });

        $('#resv_shared_form').on('submit', function (e) {
            e.preventDefault();
            reservation_request["description"] = $('#description').val();

            var start_dtstring = $('#start_at').val();
            var end_dtstring = $('#end_at').val();

            var start_m = moment(start_dtstring);
            var end_m = moment(end_dtstring);

            reservation_request["startAt"] = parseInt(start_m.unix());
            reservation_request["endAt"] = parseInt(end_m.unix());

            var json = JSON.stringify(reservation_request);

            $.ajax({
                type: "POST",
                url: "/resv/minimal_submit",
                data: json,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    console.log(data);
                },
                failure: function (errMsg) {
                    console.log(errMsg);
                }
            });


        });


    });

});


