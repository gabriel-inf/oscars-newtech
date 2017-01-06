const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');

class ReservationApp extends React.Component{

    constructor(props){
        super(props);
        let reservation = {
            junctions: [],
            pipes: []
        };
        this.state = {
            reservation: reservation,
            networkVis: {},
            resVis: {},
            showPipePanel: false,
            showJunctionPanel: false
        };
        this.componentDidMount = this.componentDidMount.bind(this);
        this.initializeNetwork = this.initializeNetwork.bind(this);
        this.initializeResGraph = this.initializeResGraph.bind(this);
        this.updateNetworkVis = this.updateNetworkVis.bind(this);
        this.handleAddJunction = this.handleAddJunction.bind(this);
        this.addToResGraph = this.addToResGraph.bind(this);
        this.addResGraphElements = this.addResGraphElements.bind(this);
        this.deleteResGraphElements = this.deleteResGraphElements.bind(this);
        this.handleSandboxSelection = this.handleSandboxSelection.bind(this);
    }

    componentDidMount(){
        this.initializeNetwork();
        this.initializeResGraph();
    }

    initializeNetwork(){
        client.loadJSON("/viz/topology/multilayer", this.updateNetworkVis);
    }

    handleAddJunction(){
        let newJunctions = this.state.networkVis.network.getSelectedNodes();
        let reservation = this.state.reservation;

        let currJunctions = reservation.junctions.slice();
        let currPipes = reservation.pipes.slice();

        let newPipes = [];

        for (let i = 0; i < newJunctions.length; i++) {
            let newNode = newJunctions[i];
            // Only add this node if it's not currently in the list
            if (currJunctions.indexOf(newNode) === -1) {
                // Add a new pipe if there's at least one current junction before addition
                // Connect previous last junction to new junction
                if(currJunctions.length > 0){
                    let lastNode = currJunctions[currJunctions.length - 1];
                    let newPipe = {
                        id: lastNode + " -- " + newNode,
                        from: lastNode,
                        to: newNode
                    };
                    currPipes.push(newPipe);
                    newPipes.push(newPipe);
                    console.log("Adding pipe: " + newPipe.id);
                }
                currJunctions.push(newNode);
                console.log("Adding junction: " + newNode);
            }
        }
        if(currJunctions.length > reservation.junctions.length){
            reservation.junctions = currJunctions;
            reservation.pipes = currPipes;
            this.setState({reservation: reservation});
            this.addToResGraph(newJunctions, newPipes);
        }
        this.state.networkVis.network.unselectAll();
    }

    updateNetworkVis(response){
        let jsonData = JSON.parse(response);
        let nodes = jsonData.nodes;
        let edges = jsonData.edges;
        let networkElement = document.getElementById('network_viz');
        let networkOptions = {
            height: '450px',
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
        let displayViz = networkVis.make_network(nodes, edges, networkElement, networkOptions, "network_viz");
        this.setState({networkVis: displayViz});
    }

    addResGraphElements(data, callback){
        if (data.from != data.to) {
            callback(data);

            let newPipe = {
                id: edgeData.from + " -- " + edgeData.to,
                from: edgeData.from,
                to: edgeData.to
            };

            let pipes = this.state.reservation.pipes.slice();
            pipes.push(newPipe);

            let reservation = this.state.reservation;
            reservation.pipes = pipes;
            this.setState({reservation: reservation});
        }
    }

    deleteResGraphElements(data, callback){
        callback(data);
        let pipes = this.state.reservation.pipes.slice();
        for(let i = 0; i < data.edges.length; i++){
            let edgeId = data.edges[i];
            let matchingPipes = $.grep(pipes, function(e){ return e.id == edgeId; });
            if(matchingPipes.length > 0){
                pipes.pop();
            }
        }

        let res = this.state.reservation;
        res.pipes = pipes;
        this.setState({reservation: res});
    }

    initializeResGraph(){
        let networkElement = document.getElementById('reservation_viz');
        let nodes = this.state.reservation.junctions;
        let edges = this.state.reservation.pipes;

        let networkOptions = {
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
                addEdge: this.addResGraphElements,
                deleteEdge: this.deleteResGraphElements,
                deleteNode: function (nodeData, callback)
                {
                    stateChanged("Node deleted.");
                }
            }
        };
        let resVis = networkVis.make_network(nodes, edges, networkElement, networkOptions, "reservation_viz");
        resVis.network.on('select', this.handleSandboxSelection);

        this.setState({resVis: resVis});
    }

    addToResGraph(newJunctions, newPipes){
        let resVis = this.state.resVis;
        resVis.datasource.edges.add(newPipes);

        let formattedJunctions = [];
        for(let i = 0; i < newJunctions.length; i++){
            let junctionName = newJunctions[i];
            let junction = {id: junctionName, label: junctionName};
            formattedJunctions.push(junction);
        }
        resVis.datasource.nodes.add(formattedJunctions);
    }

    handleSandboxSelection(params){
        let edges = params.edges;
        let nodes = params.nodes;

        if(edges.length == 0){
            this.setState({showPipePanel: false});
        }
        else{
            this.setState({showPipePanel: true});
        }
        if(nodes.length == 0){
            this.setState({showJunctionPanel: false});
        }
        else{
            this.setState({showJunctionPanel: true});
        }

    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <NetworkPanel handleAddJunction={this.handleAddJunction}/>
                <ReservationDetailsPanel reservation={this.state.reservation} showPipePanel={this.state.showPipePanel} showJunctionPanel={this.state.showJunctionPanel}/>
            </div>
        );
    }
}

class NetworkPanel extends React.Component{

    constructor(props){
        super(props);

        this.state = {showPanel: true, networkVis: {}, junctions: []};
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick(){
        this.setState({showPanel: !this.state.showPanel});
    }

    render(){
        return(
            <div className="panel-group">
                <div className="panel panel-default">
                    <Heading title={"Show / hide network"} onClick={() => this.handleHeadingClick()}/>
                    {this.state.showPanel ?
                        <div id="network_panel" className="panel-body collapse in">
                            <NetworkMap />
                            <AddNodeButton onClick={this.props.handleAddJunction}/>
                        </div> : <div />
                    }
                </div>
            </div>
        );
    }
}

class NetworkMap extends React.Component{

    constructor(props){
        super(props);
    }

    render(){
        return(
            <div id="network_viz" className="col-md-10">
                <div className="viz-network">Network map</div>
            </div>
        );
    }
}

class AddNodeButton extends React.Component{

    render(){
        return(
            <div id="add_junction_div" className="col-md-2 affix-top">
                <input type="button" id="add_junction_btn" className="btn btn-primary active" onClick={this.props.onClick} value="Add to request" />
            </div>
        );
    }
}

class ReservationDetailsPanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            showReservationPanel: true,
        };
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick(){
        this.setState({showReservationPanel: !this.state.showReservationPanel});
    }

    render(){
        console.log(this.props.reservation);
        return(
            <div className="panel-group">
                <div className="panel panel-default">
                    <Heading title={"Show / hide reservation"} onClick={() => this.handleHeadingClick()}/>
                    {this.state.showReservationPanel ?
                        <div id="reservation_panel" className="panel-body collapse collapse in">
                            <Sandbox />
                            <ParameterForm />
                        </div> : <div />
                    }
                </div>
                {this.state.showReservationPanel ?
                <div>
                    {this.props.showPipePanel ? <PipePanel /> : <div />}
                    {this.props.showJunctionPanel ? <JunctionPanel /> : <div />}
                </div> : <div />
                }
            </div>
        );
    }
}

class Heading extends React.Component{

    render(){
        return(
            <div className="panel-heading">
                <h4 className="panel-title">
                    <a href="#" onClick={() => this.props.onClick()}>{this.props.title}</a>
                </h4>
            </div>
        );
    }
}

class Sandbox extends React.Component{

    render(){
        return(
            <div id="reservation_viz" className="panel-body collapse collapse in col-md-6">
                Sandbox
            </div>
        );
    }
}

class ParameterForm extends React.Component{

    render(){
        return(
            <div id="resv_common_params_form" className="panel panel-default col-md-6">
                Parameter Form
            </div>
        );
    }
}

class PipePanel extends React.Component{

    render(){
        return(
            <div id="pipe_card" className="panel panel-default">
                Pipe details.
            </div>
        );
    }
}

class JunctionPanel extends React.Component{

    render(){
        return(
            <div id="junction_card" className="panel panel-default">
                Junction details.
            </div>
        );
    }
}

module.exports = ReservationApp;