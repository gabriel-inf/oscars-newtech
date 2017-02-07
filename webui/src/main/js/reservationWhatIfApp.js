const React = require('react');
const NavBar = require('./navbar');
import Dropdown from 'react-dropdown';
const client = require('./client');
const networkVis = require('./networkVis');
const deepEqual = require('deep-equal');
const vis = require('../../../node_modules/vis/dist/vis');


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
            maxBw: 10000,
            networkVis: {},
            bwAvailMap : {},
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
        this.updatePortMaps = this.updatePortMaps.bind(this);
        this.handleSrcPortSelect = this.handleSrcPortSelect.bind(this);
        this.handleDstPortSelect = this.handleDstPortSelect.bind(this);
        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.submitBwAvailRequest = this.submitBwAvailRequest.bind(this);
        this.initializeBandwidthMap = this.initializeBandwidthMap.bind(this);

        client.loadJSON({method: "GET", url: "/resv/newConnectionId"})
            .then(this.updateReservation);

        client.loadJSON({method: "GET", url: "/info/vlanEdges"})
            .then(this.updatePortMaps);
    }

    componentDidMount(){
        client.loadJSON({method: "GET", url: "/viz/topology/multilayer"}).then(this.initializeNetwork);
        this.initializeBandwidthMap();
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

    updatePortMaps(response){
        let vlanPorts = JSON.parse(response);

        let deviceToPortMap = {};
        deviceToPortMap["--"] = ["--"];
        let portToDeviceMap = {};

        for(let i = 0; i < vlanPorts.length; i++){
            let port = vlanPorts[i];
            let device = port.split(":")[0];
            portToDeviceMap[port] = device;
            if(deviceToPortMap.hasOwnProperty(device)){
                deviceToPortMap[device].push(port);
            }
            else{
                deviceToPortMap[device] = [port];
            }
        }
        this.setState({deviceToPortMap: deviceToPortMap});
        this.setState({portToDeviceMap: portToDeviceMap});
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

    initializeBandwidthMap(){
        let bwViz = document.getElementById('bwVisualization');

        let nowDate = Date.now();
        let furthestDate = nowDate + 1000 * 60 * 60 * 24 * 365;  // 1 year in the future

        let bwOptions = {
            style:'line',
            drawPoints: false,
            orientation:'bottom',
            start: this.state.bwAvailRequest.startTime.getUTCMilliseconds(),
            end: this.state.bwAvailRequest.endTime.getUTCMilliseconds() + (1000 * 60 * 60 * 24 * 1.1),
            zoomable: true,
            zoomMin: 1000 * 60 * 60 * 24,
            zoomMax: 1000 * 60 * 60 * 24 * 365 * 2,
            min: nowDate,
            max: furthestDate,
            shaded: {enabled: true},
            width: '90%',
            height: '400px',
            minHeight: '400px',
            maxHeight: '400px',
            legend: {enabled: false, icons: false},
            interpolation: {enabled: false},
            dataAxis: {left: {range: {min: 0, max: 10000},}, icons: true},
        };

        let bwData = new vis.DataSet();

        // Set up the look of the availability data points //
        let groupSettingsAvail = {
            id: "avail",
            content: "Group Name",
            style: 'stroke-width:1;stroke:#709FE0;',
            options: {
                shaded: {enabled: true, orientation: 'bottom', style: 'fill-opacity:0.5;fill:#709FE0;'}
            }
        };

        // Set up the look of the reservation window data points //
        let groupSettingsBar = {
            id: "bwBar",
            content: "Group Name",
            style: 'stroke-width:5;stroke:red;',
            options: {
                shaded: {enabled: true, orientation: 'bottom', style: 'fill-opacity:0.7;fill:red;'},
            }
        };

        let bwGroups = new vis.DataSet();
        bwGroups.add(groupSettingsAvail);
        bwGroups.add(groupSettingsBar);

        // Create the Bar Graph
        let bwAvailMap = new vis.Graph2d(bwViz, bwData, bwGroups, bwOptions);

        bwData.add({x: nowDate, y: -10, group: 'avail'});
        bwData.add({x: furthestDate, y: -10, group: 'avail'});

        // Set first time bar: Start Time
        let startBarID = "starttime";
        bwAvailMap.addCustomTime(this.state.bwAvailRequest.startTime, startBarID);

        // Set second time bar: End Time
        let endBarID = "endtime";
        bwAvailMap.addCustomTime(this.state.bwAvailRequest.endTime, endBarID);

        let currWindow = bwAvailMap.getWindow();

        // Listener for changing start/end times
        /*
        bwAvailMap.on('timechange', function (properties) { changeTime(properties, startBarID, endBarID); });

        // Listener for double-click event
        bwAvailMap.on('doubleClick', function (properties) { moveDatePicker(properties, startBarID, endBarID); });

        // Listener for range-change event
        bwAvailMap.on('rangechange', function (properties) { currWindow = bwAvailMap.getWindow(); });

        // Listener to redraw map as time progresses
        bwAvailMap.on('currentTimeTick', function (properties) { refreshMap(); });

        // Listener for changing bandwidth values
        $("#bwSlider").on("input change", function() { updateBandwidth(); });
        */
        this.setState({bwAvailMap: bwAvailMap});
    }

    handleBwSliderChange(event){
        this.setState({currBw: event.target.value});
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
                <div id="bwVisualization"/>
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
            <input id="bwSlider" type="range" className="verticalSlider" min="0" max={this.props.maxBw} value={this.props.bw} step="1"
                   style={{WebkitAppearance: "slider-vertical", height: "350px", width: "50px"}} onChange={this.props.handleBwSliderChange}/>
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