const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');

class ReservationApp extends React.Component{

    constructor(props){
        super(props);
        let reservation = {
            junctions: {},
            pipes: {}
        };
        this.state = {
            reservation: reservation,
            nodeOrder: [],
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
        this.addElementsToResGraph = this.addElementsToResGraph.bind(this);
        this.addPipeThroughResGraph = this.addPipeThroughResGraph.bind(this);
        this.deleteResGraphElements = this.deleteResGraphElements.bind(this);
        this.handleSandboxSelection = this.handleSandboxSelection.bind(this);
        this.deleteJunction = this.deleteJunction.bind(this);
        this.deletePipe = this.deletePipe.bind(this);
    }

    componentDidMount(){
        this.initializeNetwork();
        this.initializeResGraph();
    }

    handleAddJunction(){
        let selectedJunctions = this.state.networkVis.network.getSelectedNodes();
        let reservation = this.state.reservation;

        let newPipes = [];
        let newJunctions = [];

        let changeMade = false;
        let nodeOrder = this.state.nodeOrder.slice();
        // loop through all the selected junctions
        for (let i = 0; i < selectedJunctions.length; i++) {
            let newNodeName = selectedJunctions[i];
            // Only add this node if it's not currently in the list
            if (!reservation.junctions.hasOwnProperty(newNodeName)) {
                // Add a new pipe if there's at least one current junction before addition
                // Connect previous last junction to new junction
                if(Object.keys(reservation.junctions).length > 0){
                    let lastNodeName = nodeOrder[nodeOrder.length-1];
                    //TODO: Give all pipes a unique id
                    let newPipe = {id: lastNodeName + " -- " + newNodeName, from: lastNodeName, to: newNodeName};
                    reservation.pipes[newPipe.id] = newPipe;
                    newPipes.push(newPipe);
                }
                // Add the new junction
                let newJunction = {id: newNodeName, label: newNodeName};
                reservation.junctions[newJunction.id] = newJunction;
                newJunctions.push(newJunction);

                // A new junction has been added, update flags
                changeMade = true;
                nodeOrder.push(newNodeName);
            }
        }
        if(changeMade){
            this.setState({reservation: reservation, nodeOrder: nodeOrder});
            this.addElementsToResGraph(newJunctions, newPipes);
            this.setState({nodeOrder: nodeOrder});
        }
        this.state.networkVis.network.unselectAll();
    }

    initializeNetwork(){
        client.loadJSON("/viz/topology/multilayer", this.updateNetworkVis);
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

    initializeResGraph(){
        let networkElement = document.getElementById('reservation_viz');
        let nodes = [];
        let edges = [];

        let networkOptions = {
            height: '300px',
            interaction: {
                zoomView: true,
                dragView: true,
                selectConnectedEdges: false
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
                addEdge: this.addPipeThroughResGraph,
                deleteEdge: this.deleteResGraphElements,
                deleteNode: this.deleteResGraphElements
            },
        };
        let resVis = networkVis.make_network(nodes, edges, networkElement, networkOptions, "reservation_viz");
        resVis.network.on('select', this.handleSandboxSelection);

        this.setState({resVis: resVis});
    }

    addElementsToResGraph(newJunctions, newPipes){
        let resVis = this.state.resVis;
        resVis.datasource.edges.add(newPipes);
        resVis.datasource.nodes.add(newJunctions);
    }

    addPipeThroughResGraph(data, callback){
        //TODO: Implement pipe addition through graph
        if (data.from != data.to) {
            callback(data);

            let newPipe = {
                id: edgeData.from + " -- " + edgeData.to,
                from: edgeData.from,
                to: edgeData.to
            };


            let reservation = this.state.reservation;
            reservation.pipes[newPipe.id] = newPipe;
            this.setState({reservation: reservation});
        }
    }

    deleteResGraphElements(data, callback){
        callback(data);
        let res = this.state.reservation;
        let datasource = this.state.resVis.datasource;

        // Delete all selected pipes
        for(let i = 0; i < data.edges.length; i++){
            let edgeId = data.edges[i];
            if(res.pipes.hasOwnProperty(edgeId)) {
                this.deletePipe(res, datasource, edgeId);
            }
        }

        // Delete all selected junctions & connecting pipes
        let junctionDeleted = false;
        let nodeOrder = this.state.nodeOrder.slice();

        for(let i = 0; i < data.nodes.length; i++){
            let nodeId = data.nodes[i];

            if(res.junctions.hasOwnProperty(nodeId)){
                // Delete the junction
                this.deleteJunction(res, datasource, nodeId);

                // Delete all pipes connected to this junction
                let pipes = Object.keys(res.pipes);
                for(let j = 0; j < pipes.length; j++){
                    let edgeId = pipes[j];
                    if(edgeId.includes(nodeId)){
                        this.deletePipe(res, datasource, edgeId);
                    }
                }
                junctionDeleted = true;
                // Remove this junction from the node ordering
                let nodeIndex = nodeOrder.indexOf(nodeId);
                if(nodeIndex != -1){
                    nodeOrder.splice(nodeIndex, 1);
                }
            }
        }

        // Clear selected elements
        this.state.networkVis.network.unselectAll();
        this.handleSandboxSelection(this.state.networkVis.network.getSelection());

        this.setState({reservation: res, nodeOrder: nodeOrder});
    }

    deletePipe(res, datasource, edgeId){
        delete(res.pipes[edgeId]);
        datasource.edges.remove(edgeId);
    }

    deleteJunction(res, datasource, nodeId){
        delete(res.junctions[nodeId]);
        datasource.nodes.remove(nodeId);
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