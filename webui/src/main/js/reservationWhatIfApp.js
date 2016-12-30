const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');

class ReservationWhatIfApp extends React.Component{

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <h3>What if?</h3>
            </div>
        );
    }
}

module.exports = ReservationWhatIfApp;