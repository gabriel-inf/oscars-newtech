const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');
const networkVis = require('./networkVis');

class ReservationViewApp extends React.Component{

    constructor(props){
        super(props);

        this.state = {resViz: {}}
    }

    componentDidMount(){
        let url = "/viz/connection/" + this.props.params.connectionId;
        client.loadJSON({method: "GET", url: url})
            .then((response) => {
                    this.makeGraph(response);
                }
            );
    }

    makeGraph(response) {
        let json_data = JSON.parse(response);
        console.log(json_data);
        // Parse JSON string into object
        let nv_cont = document.getElementById('reservation_view_viz');
        let nv_opts = {
            height: '400px',
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
        let reservation_viz = networkVis.make_network_with_datasource(json_data, nv_cont, nv_opts, "reservation_view_viz");
        this.setState({resViz: reservation_viz});
    }


    triggerFormChanges(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId) {
    if (selected_a_node) {
        show_device_commands(nodeId);
    } else {
        device_commands_container.hide();
    }
}


    showDeviceCommands(nodeId) {
        console.log("showing device commands for " + connectionId + " " + nodeId);
        $('#device_commands').empty();
        device_commands_container.show();

        var url = "/resv/commands/" + connectionId + "/" +nodeId;
        loadJSON(url, function (response) {

            var commands_data = JSON.parse(response);
            console.log(commands_data);
            $('#device_commands').text(commands_data["commands"]);

        });
    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <ReservationViewBody/>
            </div>
        );
    }
}

class ReservationViewBody extends React.Component{

    render(){
        return(
            <div className="panel-group">
                <div className="panel panel-default">
                    <div className="panel-heading">
                    </div>
                    <div id="reservation_view_viz" className="panel-body collapse  collapse in"> No path data found</div>
                </div>
                <div id="device_commands_container" className="panel panel-default">
                    <div className="panel-heading">
                        <h4 className="panel-title">Device commands</h4>
                    </div>
                    <div className="panel-body">
                        <pre id="device_commands" />
                    </div>
                </div>
            </div>
        );
    }
}



module.exports = ReservationViewApp;