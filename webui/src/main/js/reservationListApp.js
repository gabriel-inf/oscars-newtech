const React = require('react');
const ReservationHeatMap = require('./reservationHeatMap');
const NavBar = require('./navbar');
const client = require('./client');
const connHelper = require('./connectionHelper');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');
const ReservationList = require("./reservationList");
import Dropdown from 'react-dropdown';

let timeoutId = -1;
class ReservationListApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            reservations: [],
            timeoutId: -1,
            filters: [],
            newFilter: {id: 0, name: "", type: "User Name"},
            filterTypes: ["Reservation Status", "Provisioning Status", "Operation Status", "Bandwidth", "User Name",
                "Start After Date", "End Before Date"],
            updateHeatMap: false
        };
        this.setState = this.setState.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.updateReservations = this.updateReservations.bind(this);
        this.evaluateReservationList = this.evaluateReservationList.bind(this);
        this.componentWillUnmount = this.componentWillUnmount.bind(this);
        this.handleFilterTypeSelect = this.handleFilterTypeSelect.bind(this);
        this.handleAddFilter = this.handleAddFilter.bind(this);
        this.handleDeleteFilter = this.handleDeleteFilter.bind(this);
        this.handleFilterNameChange = this.handleFilterNameChange.bind(this);
    }

    componentDidMount(){
        return this.updateReservations();
    }

    componentWillUnmount(){
        clearTimeout(timeoutId);
    }

    updateReservations(){
        client.loadJSON({method: "GET", url: '/resv/list/allconnections'})
            .then(
                (successResponse) => {
                    this.evaluateReservationList(successResponse);
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
        );
        timeoutId = setTimeout(this.updateReservations, 30000);   // Updates every 30 seconds
    }

    evaluateReservationList(response){
        let resvs = JSON.parse(response);
        if(connHelper.listHasChanged(this.state.reservations, resvs)){
            this.setState({reservations: resvs, updateHeatMap: true});
        }
        else{
            this.setState({updateHeatMap: false})
        }
    }


    handleFilterTypeSelect(type){
        this.setState({newFilter: {id: this.state.newFilter.id, name: this.state.newFilter.name, type: type}, updateHeatMap: false});
    }

    handleAddFilter(){
        let filter = $.extend(true, {}, this.state.newFilter);
        this.state.filters.push(filter);
        this.setState({newFilter: {id: filter.id + 1, name: "", type: filter.type}, updateHeatMap: false});
    }

    handleDeleteFilter(filter){
        let id = filter.id;
        let filteredList = this.state.filters.filter((f) => {return f.id != id});
        this.setState({filters: filteredList, updateHeatMap: false});
    }

    handleFilterNameChange(event){
        let newName = event.target.value;
        this.setState({newFilter: {id: this.state.newFilter.id, name: newName, type: this.state.newFilter.type}, updateHeatMap: false});
    }

    render() {
        return (
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <FilterPanel newFilter={this.state.newFilter}
                             filters={this.state.filters}
                             filterTypes={this.state.filterTypes}
                             handleAddFilter={this.handleAddFilter}
                             handleDeleteFilter={this.handleDeleteFilter}
                             handleFilterTypeSelect={this.handleFilterTypeSelect}
                             handleFilterNameChange={this.handleFilterNameChange}
                />
                <ReservationHeatMap updateHeatMap={this.state.updateHeatMap}/>
                <p style={{marginLeft: '40px', color: '#2c5699'}}> Select a connection to view additional reservation details.</p>
                <ReservationList reservations={this.state.reservations} alwaysExpanded={false}/>
            </div>
        );
    }
}

class FilterPanel extends React.Component{

    render(){
        return(
            <div>
                <p>Filter Reservations By: </p>
                <div style={{ display: "flex" }}>
                    <input style={{ width: "10%" }} className="form-control input-md" value={this.props.newFilter.name} onChange={this.props.handleFilterNameChange}/>
                    <Dropdown options={this.props.filterTypes}
                              value={this.props.newFilter.type}
                              placeholder="Select a filter type"
                              onChange={this.props.handleFilterTypeSelect}
                              style={{ width: "10%"}}
                    />
                    <input type="button" className="btn btn-primary" value="Add" onClick={this.props.handleAddFilter} />
                </div>
                <FilterList filters={this.props.filters} handleDeleteFilter={this.props.handleDeleteFilter}/>
            </div>
        );
    }
}


class FilterList extends React.Component{

    render(){
        let listItems = this.props.filters.map((filter) => <FilterItem key={filter.id} filter={filter} handleDeleteFilter={this.props.handleDeleteFilter}/>);
        return(
            <div style={{ display: "flex" , marginTop: "20px"}}>
                <p>Filter tags: <br/> (Click to delete)</p>
                <ul style={{ display: "flex" , flexDirection: "row", listStyle: "none"}}>{listItems}</ul>
            </div>
        );
    }
}

class FilterItem extends React.Component{

    render(){
        return(
            <li key={this.props.filter.id}
                onClick={this.props.handleDeleteFilter.bind(this, this.props.filter)}
                className="btn btn-secondary"
                style={{marginRight: "10px", backgroundColor: "lightBlue"}}
            >{this.props.filter.name}</li>
        );
    }
}


module.exports = ReservationListApp;