const React = require('react');
const ReservationMap = require('./reservationMap');
const NavBar = require('./navbar');
const loadJSON = require('./client');

class ReservationListApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {reservations: [], previousReservations: []};
        this.setState = this.setState.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
    }

    componentDidMount(){
        let resvs = [];
        loadJSON('/resv/list/allconnections', (response) =>
        {
            resvs = JSON.parse(response);
            this.setState({reservations: resvs, previousReservations: resvs});
        });
        /*client({method: 'GET', path: '/resv/list/allconnections'}).done(response => {
            let resvs = JSON.parse(response);
            this.setState({reservations: resvs, previousReservations: resvs})
        });*/
    }

    render() {
        return (
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <ReservationMap reservations={this.state.reservations}/>
                <ReservationList reservations={this.state.reservations}/>
            </div>
        );
    }
}

module.exports = ReservationListApp;

class ReservationList extends React.Component{


    render(){
        let reservations = this.props.reservations.map(resv =>
            <ReservationListItem reservation={resv} key={resv.connectionId}/>
        );
        return (
            <table id="listTable" className="table table-hover">
                <thead>
                <tr>
                    <th id="tester" style={{marginLeft: '40px', color: '#2c5699'}}> Expand a connection to view additional reservation details.</th>
                </tr>
                <tr>
                    <th style={{width: '150px'}}>Connection ID</th>
                    <th style={{width: '200px'}}>Description</th>
                    <th style={{width: '200px'}}>Status</th>
                    <th style={{width: '200px'}}>Request Details</th>
                    <th style={{width: '200px'}}>User</th>
                    <th style={{width: '200px'}}>Submission Time</th>
                </tr>
                </thead>
                <tbody id="listBody">
                {reservations}
                </tbody>
            </table>
        )
    }
}

class ReservationListItem extends React.Component{

    constructor(props){
        super(props);
        this.state = {startDate: new Date(), endDate: new Date(), submitDate: new Date()};
    }

    componentDidMount(){
        let start = this.state.startDate;
        start.setTime(this.props.reservation.reservedSchedule[0]);
        let end = this.state.endDate;
        end.setTime(this.props.reservation.reservedSchedule[1]);
        let submit = this.state.submitDate;
        submit.setTime(this.props.reservation.schedule.submitted);
        this.setState({startDate: start, endDate: end, submitDate: submit});
    }

    render(){
        return(
            <tr className="accordion-toggle">
                <td>{this.props.reservation.connectionId}</td>
                <td>{this.props.reservation.specification.description}</td>
                <td>
                    <div>{this.props.reservation.states.resv}</div>
                    <div>{this.props.reservation.states.prov}</div>
                    <div>{this.props.reservation.states.oper}</div>
                </td>
                <td>
                    <div>{"Start: " + this.state.startDate.toString()}</div>
                    <div>{"End: " + this.state.endDate.toString()}</div>
                </td>
                <td>{this.props.reservation.specification.username}</td>
                <td>{this.state.submitDate.toString()}</td>
            </tr>
        )

    }
}


// Used to determine if reservations from previous refresh have changed //
function listHasChanged(oldConnectionList, newConnectionList)
{
    // Won't slow things down if newConnectionList is also empty
    if($.isEmptyObject(oldConnectionList))
        return true;

    // Same size
    if(oldConnectionList.length !== newConnectionList.length)
        return true;

    // Same Reservations - All properties unchanged
    for(let o = 0; o < oldConnectionList.length; o++)
    {
        let oldConn = oldConnectionList[o];

        let newIndex = connectionIndex(oldConn, newConnectionList);

        if(newIndex === -1)
            return true;
    }

    return false;
}


/* Identify outdated connections and remove them from Reservation List DOM Table */
function removeOldConnections(oldConnectionList, newConnectionList)
{
    let connsToRemove = [];

    for(let o = 0; o < oldConnectionList.length; o++)
    {
        let oldConn = oldConnectionList[o];

        let newIndex = connectionIndex(oldConn, newConnectionList);

        if(newIndex === -1)
            connsToRemove.push(oldConn);
    }

    console.log("ConnsToRemove Size: " + connsToRemove.length);

    for(let c = 0; c < connsToRemove.length; c++)
    {
        let deadConn = connsToRemove[c];

        let listBody = document.getElementById('listBody');
        let connectionRow = document.getElementById("row_" + deadConn.connectionId);
        let hiddenRow = document.getElementById("hidden_" + deadConn.connectionId);

        listBody.removeChild(connectionRow);
        listBody.removeChild(hiddenRow);
    }
}

/* Disregards any unchanged connections and returns a set of exclusively new or updated connections -- Used to prevent complete renewal of connection list table */
function getNewConnections(oldConnectionList, newConnectionList)
{
    let newConnections = [];

    for(let n = 0; n < newConnectionList.length; n++)
    {
        let newConn = newConnectionList[n];

        let oldIndex = connectionIndex(newConn, oldConnectionList);

        if(oldIndex === -1)
            newConnections.push(newConn);
    }

    return newConnections;
}

/* Populates and Refreshes list of connections in the DOM table -- Refreshes automatically */
function initializeConnectionList()
{
    console.log("Refreshing Connections...");

    let previousConnections = filteredConnections.slice();
    filteredConnections = [];
    filteredConnectionIDs = [];

    loadJSON("/resv/list/allconnections", function (response)
    {
        filteredConnections = JSON.parse(response);

        filteredConnections.forEach(function(conn){ filteredConnectionIDs.push(conn.connectionId); });

        // Do nothing if no connections have been added/removed/updated since last refresh //
        if(!listHasChanged(previousConnections, filteredConnections))
        {
            console.log("NO CHANGE");
            return;
        }

        // Delete rows from DOM Table for removed/outdated connections //
        removeOldConnections(previousConnections, filteredConnections);

        // Add rows to DOM Table only for added/updated connections //
        let newConnections = getNewConnections(previousConnections, filteredConnections);


        getAllReservedBWs();
    });

    setTimeout(initializeConnectionList, 30000);   // Updates every 30 seconds
}

function showDetails(connectionToShow)
{
    let connID = connectionToShow.id.split("accordion_");

    drawReservation(connID[1]);
}

function clearDetails(connectionToShow)
{
    let connID = connectionToShow.id.split("accordion_");

    clearReservation(connID[1]);
}

function drawReservation (connectionID)
{
    let vizName = "resViz_" + connectionID;
    let emptyVizName = "emptyViz_" + connectionID;

    let vizElement = document.getElementById(vizName);
    let emptyVizElement = document.getElementById(emptyVizName);

    loadJSON("/viz/connection/" + connectionID, function (response)
    {
        let json_data = JSON.parse(response);
        console.log(json_data);

        edges = json_data.edges;
        nodes = json_data.nodes;

        if(edges.length === 0 || nodes.length === 0)
        {
            $(vizElement).hide();
            $(emptyVizElement).show();
            return;
        }
        else
        {
            $(vizElement).show();
            $(emptyVizElement).hide();
        }

        // Parse JSON string into object
        let resOptions = {
            autoResize: true,
            width: '100%',
            height: '100%',
            interaction: {
                hover: true,
                navigationButtons: false,
                zoomView: false,
                dragView: false,
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

        reservation_viz = make_network(json_data, vizElement, resOptions, vizName);
    });
}

$(function ()
{
    let token = $("meta[name='_csrf']").attr("content");
    let header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });
});