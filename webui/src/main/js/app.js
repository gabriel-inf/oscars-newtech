'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const Router = require('react-router').Router;
const Route = require('react-router').Route;
const Link = require('react-router').Link;
const browserHistory = require('react-router').browserHistory;
const client = require('./client');
const NavBar = require('./navbar');
const ReservationListApp = require('./reservationListApp');

//TODO: Find way to authenticate user
let isAuthenticated = false;
let isAdmin = false;
// end::vars[]

// tag::app[]
class App extends React.Component {

    render() {
        return (
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <div>This is an App.</div>
            </div>
        );
    }
}
// end::app[]

$(function ()
{
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });
});

// tag::render[]
ReactDOM.render(<Router history={browserHistory}>
                    <Route path="/react" isAuthenticated={isAuthenticated} isAdmin={isAdmin} component={ReservationListApp}> </Route>
                </Router>
                 ,document.getElementById('react') );
// end::render[]
