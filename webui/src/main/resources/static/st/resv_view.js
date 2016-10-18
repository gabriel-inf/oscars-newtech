var reservation_viz;

var make_graphs = function() {

    loadJSON("/viz/connection/"+connectionId, function (response) {

        var json_data = JSON.parse(response);
        console.log(json_data);

        // Parse JSON string into object
        var nv_cont = document.getElementById('reservation_view_viz');
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
        reservation_viz = make_network(json_data, nv_cont, nv_opts, "reservation_view_viz");
    });
};

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId) {

}


$(document).ready(function () {

    make_graphs();



    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });
});
