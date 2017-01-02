const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');

class ReservationApp extends React.Component{

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <h3>Make a reservation.</h3>
            </div>
        );
    }
}

module.exports = ReservationApp;