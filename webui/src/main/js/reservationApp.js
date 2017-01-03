const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');

class ReservationApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {reservation: {}}
    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <NetworkPanel />
                <ReservationDetailsPanel reservation={this.state.reservation}/>
            </div>
        );
    }
}

class NetworkPanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {showPanel: true}
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
                            <AddNodeButton />
                        </div> : <div />
                    }
                </div>
            </div>
        );
    }
}

class NetworkMap extends React.Component{

    render(){
        return(
            <div id="network_viz" className="col-md-10">
                <div className="vix-network">Network map</div>
            </div>
        );
    }
}

class AddNodeButton extends React.Component{

    render(){
        return(
            <div id="add_junction_div" className="col-md-2 affix-top">
                <button type="submit" id="add_junction_btn" className="btn btn-primary active">Add to request</button>
            </div>
        );
    }
}

class ReservationDetailsPanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {showReservationPanel: true};
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
                    <PipePanel />
                    <JunctionPanel />
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