const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');

class ReservationWhatIfApp extends React.Component{

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <h2> What-if? </h2>
                <NetworkMapPanel />
                <BandwidthTimePanel />
            </div>
        );
    }
}

class NetworkMapPanel extends React.Component{


    render(){
        return(
            <div className="panel-group" >
                <div className="panel panel-default">
                    <Heading title="Show / hide network" />
                    <div id="network_panel" className="panel-body collapse in">
                        <div id="networkVisualization" className="col-md-10" width="90%">
                            <div className="vis-network" />
                        </div>
                        <div>
                            <div>

                            </div>
                            <div className="dropdown">
                                <p style="font-size:16px">Select Source Port</p>
                                <button type="button" id="srcPortDrop" className="btn dropdown-toggle">
                                    <span className="caret" />
                                </button>
                                <ul className="dropdown-menu" id="srcPortList" />
                            </div>
                            <div className="dropdown">
                                <p style="font-size:16px">Select Source Port</p>
                                <button type="button" id="dstPortDrop" className="btn dropdown-toggle">
                                    <span className="caret" />
                                </button>
                                <ul className="dropdown-menu" id="dstPortList" />
                            </div>
                        </div>
                    </div>
                </div>
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