const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');


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

        client.loadJSON({method: "GET", url: "/resv/newConnectionId"})
            .then((response) => {
                    let json_data = JSON.parse(response);
                    reservation["connectionId"] = json_data["connectionId"];
                }
            );
        this.state = {
            reservation: reservation,
            maxBw: 100,
            bw: 50
        };

        this.handleBwSliderChange = this.handleBwSliderChange.bind(this);
    }

    handleBwSliderChange(event){
        this.setState({bw: event.target.value});
    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <h2> What-if? </h2>
                <PathSelectionPanel />
                <BandwidthTimePanel handleBwSliderChange={this.handleBwSliderChange} maxBw={this.state.maxBw} bw={this.state.bw}/>
                <ParameterDisplay bw={this.state.bw} startAt={this.state.reservation.startAt} endAt={this.state.reservation.endAt}/>
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
                    <NetworkPanel show={this.state.showPanel}/>
                </div>
            </div>
        );
    }
}

class NetworkPanel extends React.Component{

    render(){
        return(
            <div id="network_panel" className="panel-body collapse in" style={this.props.show ? {} : { display: "none" }}>
                <div id="networkVisualization" className="col-md-10" width="90%">
                    <div className="vis-network" />
                </div>
                <div>
                    <div>
                        <button type="reset" id="buttonCancelERO" className="btn btn-danger disabled">Clear Path</button>
                    </div>
                    <PortSelectionDropdown type="src" />
                    <PortSelectionDropdown type="dst" />
                </div>
            </div>
        );
    }
}

class PortSelectionDropdown extends React.Component{

    render(){
        let id = this.props.type + "PortDrop";
        let ulId = this.props.type + "PortList";
        return(
            <div className="dropdown">
                <p style={{fontSize: "16px"}}>Select Source Port</p>
                <button type="button" id={id} className="btn dropdown-toggle">
                    <span className="caret" />
                </button>
                <ul className="dropdown-menu" id={ulId} />
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