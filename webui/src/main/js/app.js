'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom')
const client = require('./client');
const NavBar = require('./navbar');
// end::vars[]

// tag::app[]
class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {isAuthenticated: true, isAdmin: true};
    }

    render() {
        return (
            <NavBar isAuthenticated={this.state.isAuthenticated} isAdmin={this.state.isAdmin}/>
        );
    }
}
// end::app[]


// tag::render[]
ReactDOM.render(
<App />,
    document.getElementById('react')
);
// end::render[]
