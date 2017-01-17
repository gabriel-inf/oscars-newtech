const React = require('react');
const NavBar = require('./navbar');

class ReservationViewApp extends React.Component{

    render(){
        {this.props.params.connectionId}
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <ReservationGraph reservation={reservation} handleAddJunction={this.handleAddJunction}/>
            </div>
        );
    }
}



module.exports = ReservationViewApp;