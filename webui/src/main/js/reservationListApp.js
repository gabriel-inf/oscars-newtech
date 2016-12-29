const React = require('react');
const ReservationMap = require('./reservationMap');
const NavBar = require('./navbar');
const loadJSON = require('./client');
const connHelper = require('./connectionHelper');
const networkVis = require('./networkVis');

class ReservationListApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {reservations: []};
        this.setState = this.setState.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.updateReservations = this.updateReservations.bind(this);
        this.listHasChanged = this.listHasChanged.bind(this);
    }

    componentDidMount(){
        return this.updateReservations();
    }

    updateReservations(){
        loadJSON('/resv/list/allconnections', (response) =>
        {
            let resvs = JSON.parse(response);
            if(this.listHasChanged(this.state.reservations, resvs)){
                this.setState({reservations: resvs});
            }
        });

        setTimeout(this.updateReservations, 10000);   // Updates every 30 seconds
    }

    listHasChanged(oldConnectionList, newConnectionList) {
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

            let newIndex = connHelper.connectionIndex(oldConn, newConnectionList);

            if(newIndex === -1)
                return true;
        }

        return false;
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

class ReservationList extends React.Component{
    constructor(props){
        super(props);

        let resExpanded = {};
        for(let resv of this.props.reservations){
            resExpanded[resv.connectionId] = false;
        }

        this.state = {resExpanded: resExpanded};
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick(id){
        let resExpanded = this.state.resExpanded;
        resExpanded[id] = !resExpanded[id];
        this.setState({resExpanded: resExpanded});
    }

    render(){
        const rows = [];
        for(let resv of this.props.reservations){
            rows.push(
                <ReservationDetails reservation={resv} key={resv.connectionId} onClick={(id) => this.handleClick(id)}/>
            );
            if(this.state.resExpanded[resv.connectionId]){
                let graphKey = "graph_" + resv.connectionId;
                rows.push(
                    <ReservationGraph connId={resv.connectionId} key={graphKey}/>
                );
            }
        }
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
                {rows}
                </tbody>
            </table>
        )
    }
}


class ReservationDetails extends React.Component{
    constructor(props){
        super(props);
        this.state = {startDate: new Date(), endDate: new Date(), submitDate: new Date(), graphHidden: true};
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
            <tr className="accordion-toggle" onClick={() => this.props.onClick(this.props.reservation.connectionId)} >
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

class ReservationGraph extends React.Component{

    constructor(props){
        super(props);
        this.state = {edges: [], nodes: []}
    }

    componentDidMount(){
        loadJSON("/viz/connection/" + this.props.connId, (response) =>
        {
            let json_data = JSON.parse(response);

            let edges = json_data.edges;
            let nodes = json_data.nodes;
            this.setState({edges: edges, nodes: nodes});

            if(edges.length == 0 || nodes.length === 0){
                return;
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

            let vizName = "resViz_" + this.props.connId;

            let vizElement = document.getElementById(vizName);
            let reservation_viz = networkVis.make_network(json_data, vizElement, resOptions, vizName);
        });
    }

    render(){
        let resVizId = "resViz_" + this.props.connId;
        let resVizClass = "panel-body in" + this.props.connId;
        return (
            <tr>
                <td colSpan={6}>
                    {this.state.edges.length > 0 && this.state.nodes.length > 0 ?
                        <div>
                            <b>Reservation Path:</b> {this.props.connId}
                            <div id={resVizId} className={resVizClass} style={{display:"block"}}></div>
                        </div> :
                        <div>
                            <b>Reservation Path:</b> {this.props.connId}
                            <div>No path data found.</div>
                        </div>
                    }
                </td>
            </tr>
        );
    }
}


module.exports = ReservationListApp;