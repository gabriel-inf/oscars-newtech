const vis = require('../../../node_modules/vis/dist/vis');

let resv_viz_name = "reservation_viz";

function make_network(nodes, edges, container, options, name) {
    // datasource{ nodes: vis.DataSet(nodes), edges -> vis.DataSet(edges) }

    let nodeDataset = new vis.DataSet(nodes);
    let edgeDataset = new vis.DataSet(edges);
    let datasource = {
        nodes: nodeDataset,
        edges: edgeDataset
    };

    return make_network(datasource, container, options, name);
}

function make_network_with_datasource(datasource, container, options, name) {
    // datasource{ nodes: vis.DataSet(nodes), edges -> vis.DataSet(edges) }


    let network = new vis.Network(container, datasource, options);

    let result = {};
    result.network = network;
    result.datasource = datasource;

    attach_handlers(network, datasource, name);
    return result;
}

function attach_handlers(vis_js_network, vis_js_datasets, name) {

    let selected_node_ids = {};
    vis_js_network.on('dragEnd', function (params) {
        selected_node_ids.name = [];
        for (let i = 0; i < params.nodes.length; i++) {
            let nodeId = params.nodes[i];

            selected_node_ids.name.push(nodeId);
            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: true, y: true}});
            }
        }
    });

    vis_js_network.on('dragStart', function (params) {
        let draggedPlain = false;
        selected_node_ids.name = [];
        for (let i = 0; i < params.nodes.length; i++) {
            let nodeId = params.nodes[i];
            selected_node_ids.name.push(nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: false, y: false}});
                draggedPlain = true;
            }
        }

        let is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }

    });

    vis_js_network.on("click", function (params) {
        let clickedNode = false;
        let clickedEdge = false;
        let clickedPlain = false;
        let edgeId = "";
        let nodeId = "";
        selected_node_ids.name = [];
        let i;

        for (i = 0; i < params.nodes.length; i++) {
            clickedNode = true;
            nodeId = params.nodes[i];

            if (!vis_js_network.isCluster(nodeId) == true) {
                clickedPlain = true;
            }
            selected_node_ids.name.push(nodeId);
        }

        let is_resv = false;
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

function highlight_devices(network, deviceIDs, isSelected, color)
{
    let newColor = color;
    let normalNodeColor = "white";

    if($.isEmptyObject(deviceIDs))
        return;

    for(let d = 0; d < deviceIDs.length; d++)
    {
        if(isSelected)
            network.nodes.update([{id: deviceIDs[d], color: {background: newColor}}]);
        else
            network.nodes.update([{id: deviceIDs[d], color: {background: normalNodeColor}}]);
    }
}

function highlight_links(network, linkIDs, isSelected, color)
{
    let newColor = color;
    let normalEdgeColor = "#2F7FED";

    if($.isEmptyObject(linkIDs))
        return;

    for(let l = 0; l < linkIDs.length; l++)
    {
        if(isSelected)
            network.edges.update([{id: linkIDs[l], color: {color: newColor}}]);
        else
            network.edges.update([{id: linkIDs[l], color: {color: normalEdgeColor}}]);
    }
}

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId)
{
    //TODO: Implement some actions when parts of reservation viz are selected
    ;
}

module.exports = {make_network, make_network_with_datasource};