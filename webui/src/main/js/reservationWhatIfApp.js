const React = require('react');
const NavBar = require('./navbar');
import Dropdown from 'react-dropdown';
const client = require('./client');
const networkVis = require('./networkVis');
const deepEqual = require('deep-equal');


class ReservationWhatIfApp extends React.Component{

    constructor(props){
        super(props);
        // Junction: {id: ~~, label: ~~, fixtures: {}}
        // fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
        // Pipe: {id: ~~, a: ~~, z: ~~, bw: ~~}

        // Initialize default start/end date times
        let startAt = new Date();
        startAt.setTime(startAt.getTime() + 5000 * 60);
        let endAt = new Date();
        endAt.setDate(endAt.getDate() + 1);
        endAt.setTime(endAt.getTime() + 15000 * 60);

        let reservation = {
            junctions: {},
            pipes: {},
            startAt: startAt,
            endAt: endAt,
            description: "",
            connectionId: "",
            status: "UNHELD"
        };

        let bwAvailRequest = {
            startTime: startAt,
            endTime: endAt,
            azERO: [],
            zaERO: [],
            azBandwidth: 0,
            zaBandwidth: 0,
        };


        this.state = {
            reservation: reservation,
            bwAvailRequest: bwAvailRequest,
            src: "--",
            srcPort: "--",
            dst: "--",
            dstPort: "--",
            currBw: 0,
            maxBw: 100,
            networkVis: {},
            pathClearEnabled: false,
            portToDeviceMap: {"--" : "--"},
            deviceToPortMap: {["--"] : ["--"]},
            networkPortMap: {}
        };

        this.handleBwSliderChange = this.handleBwSliderChange.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.initializeNetwork = this.initializeNetwork.bind(this);
        this.selectNode = this.selectNode.bind(this);
        this.handleClearPath = this.handleClearPath.bind(this);
        this.updateReservation = this.updateReservation.bind(this);
        this.updatePtDMap = this.updatePtDMap.bind(this);
        this.updateDtPMap = this.updateDtPMap.bind(this);
        this.handleSrcPortSelect = this.handleSrcPortSelect.bind(this);
        this.handleDstPortSelect = this.handleDstPortSelect.bind(this);
        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.submitBwAvailRequest = this.submitBwAvailRequest.bind(this);

        client.loadJSON({method: "GET", url: "/resv/newConnectionId"})
            .then(this.updateReservation);

        client.loadJSON({method: "GET", url: "/topology/portdevicemap/full"})
            .then(this.updatePtDMap);

        client.loadJSON({method: "GET", url: "/topology/deviceportmap/full"})
            .then(this.updateDtPMap);
    }

    componentDidMount(){
        client.loadJSON({method: "GET", url: "/viz/topology/multilayer"}).then(this.initializeNetwork);
    }

    componentDidUpdate(prevProps, prevState){
        // Only do verification and bw avail request if relevant state has changed
        let bwAvailRequest = this.state.bwAvailRequest;
        let pathChange = !deepEqual(prevState.bwAvailRequest, bwAvailRequest);
        let portChange = prevState.srcPort != this.state.srcPort || prevState.dstPort != this.state.dstPort;
        if(bwAvailRequest.azERO.length > 1 && (pathChange || portChange)){
            let bwAvailResponse = this.submitBwAvailRequest(bwAvailRequest);
            bwAvailResponse.then(
                (successResponse) => {
                    console.log(successResponse);
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
            );
        }
    }

    submitBwAvailRequest(bwAvailRequest){
        // Add src and dst ports: [srcPort, azERO, dstPort], [dstPort, zaERO, srcPort]
        let modAzERO = bwAvailRequest.azERO.slice();
        let modZaERO = bwAvailRequest.zaERO.slice();
        modAzERO.unshift(this.state.srcPort);
        modAzERO.push(this.state.dstPort);
        modZaERO.unshift(this.state.dstPort);
        modZaERO.push(this.state.srcPort);
        let modBwAvailRequest = {
            startTime: bwAvailRequest.startTime,
            endTime: bwAvailRequest.endTime,
            azERO: modAzERO,
            zaERO: modZaERO,
            azBandwidth: bwAvailRequest.azBandwidth,
            zaBandwidth: bwAvailRequest.zaBandwidth,
        };
        return client.submit("POST", "/topology/bwavailability/path", modBwAvailRequest);
    }

    updateReservation(response){
        let jsonData = JSON.parse(response);
        let reservation = this.state.reservation;
        reservation["connectionId"] = jsonData["connectionId"];
        this.setState({reservation: reservation});
    }

    updatePtDMap(response){
        let map = JSON.parse(response);
        this.setState({portToDeviceMap: map});
    }

    updateDtPMap(response){
        let map = JSON.parse(response);
        map["--"] = ["--"];
        this.setState({deviceToPortMap: map});
    }

    initializeNetwork(response){
        let jsonData = JSON.parse(response);
        let nodes = jsonData.nodes;
        let edges = jsonData.edges;
        let networkElement = document.getElementById('network_viz');
        let netOptions = {
            autoResize: true,
            width: '90%',
            height: '400px',
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: false,
                dragView: true,
                multiselect: false,
                selectable: true,
            },
            physics: {
                stabilization: true,
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"},
            }
        };
        let displayViz = networkVis.make_network(nodes, edges, networkElement, netOptions, "network_viz");
        displayViz.network.on('selectNode', this.selectNode);
        this.setState({networkVis: displayViz});
    }

    selectNode(properties){
        if(properties.nodes.length === 0)       // Only consider node clicks
            return;

        let theNode = properties.nodes[0];

        // Make a clone of BW Avail request so you can detect changes more easily
        let bwAvailRequest = jQuery.extend(true, {}, this.state.bwAvailRequest);

        let azERO = bwAvailRequest.azERO.slice();
        let removedNodes = [];

        let index = $.inArray(theNode, azERO);

        // New selection, add to list
        if(index == -1){
            azERO.push(theNode);
        }
        // Reselected, remove from list
        else{
            removedNodes.push(theNode);
            azERO.splice(index, 1);
        }

        let datasource = this.state.networkVis.datasource;
        this.state.networkVis.network.unselectAll();
        networkVis.highlight_devices(datasource, azERO, true, 'green');
        networkVis.highlight_devices(datasource, removedNodes, false, '');

        let pathClearEnabled = false;
        let src = "--";
        let dst = "--";
        if(azERO.length > 0) {
            pathClearEnabled = true;
            src = azERO[0];
        }
        if(azERO.length > 1){
            dst = azERO[azERO.length-1];
        }
        let zaERO = azERO.slice().reverse();

        let prevSrc = this.state.portToDeviceMap[this.state.srcPort];
        let prevDst = this.state.portToDeviceMap[this.state.dstPort];
        let srcPort = "--";
        if(src == prevDst){
            srcPort = this.state.dstPort;
        }
        else{
            srcPort = prevSrc != src ? this.state.deviceToPortMap[src][0]: this.state.srcPort;
        }
        let dstPort = prevDst != dst ? this.state.deviceToPortMap[dst][0]: this.state.dstPort;

        bwAvailRequest["azERO"] = azERO;
        bwAvailRequest["zaERO"] = zaERO;
        this.setState({bwAvailRequest, pathClearEnabled: pathClearEnabled, src: src, srcPort: srcPort, dst: dst, dstPort: dstPort});
    }

    handleBwSliderChange(event){
        this.setState({currBw: event.target.value});
    }

    handleClearPath(){
        this.state.networkVis.network.unselectAll();
        let bwAvailRequest = this.state.bwAvailRequest;
        networkVis.highlight_devices(this.state.networkVis.datasource, this.state.bwAvailRequest.azERO, false, '');
        bwAvailRequest["azERO"] = [];
        bwAvailRequest["zaERO"] = [];
        this.setState({bwAvailRequest: bwAvailRequest, currBw: 0, maxBw: 100, pathClearEnabled: false, src: "--", srcPort: "--", dst: "--", dstPort: "--"});
    }

    handleSrcPortSelect(option){
        this.setState({srcPort: option.value});
    }

    handleDstPortSelect(option){
        this.setState({dstPort: option.value});
    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <h2> What-if? </h2>
                <PathSelectionPanel
                    pathClearEnabled={this.state.pathClearEnabled}
                    handleClearPath={this.handleClearPath}
                    srcPorts={this.state.deviceToPortMap[this.state.src]}
                    srcPort={this.state.srcPort}
                    dstPorts={this.state.deviceToPortMap[this.state.dst]}
                    dstPort={this.state.dstPort}
                    handleSrcPortSelect={this.handleSrcPortSelect}
                    handleDstPortSelect={this.handleDstPortSelect}
                />
                <BandwidthTimePanel
                    handleBwSliderChange={this.handleBwSliderChange}
                    maxBw={this.state.maxBw}
                    bw={this.state.currBw}
                />
                <ParameterDisplay bw={this.state.currBw} startAt={this.state.reservation.startAt} endAt={this.state.reservation.endAt}/>
                <ReservationButton />
            </div>
        );
    }
}

class PathSelectionPanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            showPanel: true,
        };
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick(){
        this.setState({showPanel: !this.state.showPanel});
    }

    render(){
        return(
            <div className="panel-group" >
                <div className="panel panel-default">
                    <Heading title="Show / hide network" onClick={this.handleHeadingClick}/>
                    <NetworkPanel
                        show={this.state.showPanel}
                        pathClearEnabled={this.props.pathClearEnabled}
                        handleClearPath={this.props.handleClearPath}
                        srcPorts={this.props.srcPorts}
                        srcPort={this.props.srcPort}
                        dstPorts={this.props.dstPorts}
                        dstPort={this.props.dstPort}
                        handleSrcPortSelect={this.props.handleSrcPortSelect}
                        handleDstPortSelect={this.props.handleDstPortSelect}
                    />
                </div>
            </div>
        );
    }
}

class NetworkPanel extends React.Component{

    render(){

        let buttonStatus = this.props.pathClearEnabled? "active" : "disabled";
        let buttonClassName = "btn btn-danger " + buttonStatus;
        return(
            <div id="network_panel" className="panel-body collapse in" style={this.props.show ? {} : { display: "none" }}>
                <div id="network_viz" className="col-md-10" width="90%">
                    <div className="viz-network">Network map</div>
                </div>
                <div>
                    <div>
                        <button type="reset"
                                id="buttonCancelERO"
                                className={buttonClassName}
                                onClick={this.props.handleClearPath}>
                            Clear Path
                        </button>
                    </div>
                </div>
                <div>
                    <div className="dropdown">
                        <p style={{fontSize: "16px"}}>Select Source Port</p>
                        <Dropdown options={this.props.srcPorts} value={this.props.srcPort} placeholder="Select a port" onChange={this.props.handleSrcPortSelect}/>
                    </div>

                    <div className="dropdown">
                        <p style={{fontSize: "16px"}}>Select Destination Port</p>
                        <Dropdown options={this.props.dstPorts} value={this.props.dstPort} placeholder="Select a port" onChange={this.props.handleDstPortSelect}/>
                    </div>
                </div>
            </div>
        );
    }
}

class PortSelectionDropdown extends React.Component{

    render(){
        let id = this.props.type + "PortDrop";
        let ulId = this.props.type + "PortList";
        let title = this.props.type === "src"? "Select Source Port" : "Select Destination Port";
        let listItems = this.props.list.map((port) => <li key={port}>{port}</li>);
        debugger;
        return(
            <div className="dropdown">
                <p style={{fontSize: "16px"}}>{title}</p>
                <button type="button" id={id} className="btn dropdown-toggle">
                    <span className="caret" />
                </button>
                <ul className="dropdown-menu" id={ulId}>
                    {listItems}
                </ul>
            </div>
        );
    }
}

class BandwidthTimePanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            showPanel: true,
        };
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick(){
        this.setState({showPanel: !this.state.showPanel});
    }

    render(){
        return(
            <div className="panel-group">
                <div className="panel panel-default">
                    <Heading title="Show / hide Bandwidth x Time Availability" onClick={this.handleHeadingClick}/>
                    <AvailabilityPanel show={this.state.showPanel} handleBwSliderChange={this.props.handleBwSliderChange} maxBw={this.props.maxBw} bw={this.props.bw}/>
                </div>
            </div>
        );
    }
}


class AvailabilityPanel extends React.Component{
    render(){
        return(
            <div id="availability_panel" className="panel-body collapse  collapse in" style={this.props.show ? {} : { display: "none" }}>
                <MessageBox
                    className="alert alert-danger"
                    message="Select both a source and destination port to display the bandwidth availability of your selected route! "
                />
                <div style={{float: "left"}}>
                    <BandwidthSlider handleBwSliderChange={this.props.handleBwSliderChange} maxBw={this.props.maxBw} bw={this.props.bw}/>
                </div>
                <div id="bwVisualization" />
            </div>
        );
    }
}

class MessageBox extends React.Component{

    render(){
        return(<div id="errors_box" className={this.props.messageBoxClass}>{this.props.message}</div>
        );
    }
}

class BandwidthSlider extends React.Component{

    render(){
        return(
            <input id="bwSlider" type="range" className="verticalSlider" min="0" max={this.props.maxBw} value={this.props.bw} step="1" height="400px" width="5px"
                   style={{WebkitAppearance: "slider-vertical"}} onChange={this.props.handleBwSliderChange}/>
        );
    }
}

class ParameterDisplay extends React.Component{

    render(){
        let bandwidthText = this.props.bw + " Mb/s";
        let startText = this.props.startAt.toString();
        let endText = this.props.endAt.toString();
        return(
            <div style={{width: "50%", margin: "auto"}}>
                <ParameterDiv heading="Bandwidth: " value={bandwidthText}/>
                <p />
                <ParameterDiv heading="Start: " value={startText}/>
                <p />
                <ParameterDiv heading="End: " value={endText}/>
            </div>
        );
    }
}

class ParameterDiv extends React.Component{

    render(){
        return(
            <div className="paramdisplay">
                <p className="paramdisplay">{this.props.heading}</p>
                <p className="paramdisplay">{this.props.value}</p>
            </div>
        );
    }
}

class ReservationButton extends React.Component{

    render(){
        return(
            <div style={{width: "40%", margin: "auto", padding: "auto"}}>
                <button
                    id="buttonHold"
                    className="btn btn-primary disabled"
                    style={{fontSize: "25px", border: "none", cursor: "pointer", minWidth: "300px"}}>
                    Place Reservation
                </button>
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

module.exports = ReservationWhatIfApp;