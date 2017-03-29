const React = require('react');
const ReservationHeatMap = require('./reservationHeatMap');
const NavBar = require('./navbar');
const client = require('./client');
const connHelper = require('./connectionHelper');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');
const ReservationList = require("./reservationList");
import Dropdown from 'react-dropdown';
const DateTime = require('react-datetime');

let timeoutId = -1;
let resStatusFilter = "Reservation Status";
let provStatusFilter = "Provision Status";
let operStatusFilter = "Operation Status";
let bandwidthFilter = "Bandwidth";
let userNameFilter = "User Name";
let startFilter = "Start After Date";
let endFilter = "End Before Date";
let idFilter = "Connection ID";

class ReservationListApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            reservations: [],
            timeoutId: -1,
            filters: [],
            newFilter: {id: 0, text: "", type: "Connection ID"},
            filterTypes: [idFilter, userNameFilter, bandwidthFilter, startFilter, endFilter, resStatusFilter,
                provStatusFilter, operStatusFilter],
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
        this.handleFilterTextChange = this.handleFilterTextChange.bind(this);
        this.handleFilterDateChange = this.handleFilterDateChange.bind(this);
    }

    componentDidMount(){
        return this.updateReservations();
    }

    componentWillUnmount(){
        clearTimeout(timeoutId);
    }

    updateReservations(){
        let combinedFilter = this.makeCombinedFilter(this.state.filters);
        client.submit("POST", '/resv/list/filter', combinedFilter)
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

    makeCombinedFilter(filters){
        let combinedFilter = {
            userNames: [],
            connectionIds: [],
            bandwidths: [],
            startDates: [],
            endDates: [],
            resvStates: [],
            provStates: [],
            operStates: []
        };
        for(let i = 0; i < filters.length; i++){
            let filter = filters[i];
            switch(filter.type){
                case userNameFilter:
                    combinedFilter.userNames.push(filter.text);
                    break;
                case bandwidthFilter:
                    combinedFilter.bandwidths.push(filter.text);
                    break;
                case idFilter:
                    combinedFilter.connectionIds.push(filter.text);
                    break;
                case startFilter:
                    combinedFilter.startDates.push(filter.text);
                    break;
                case endFilter:
                    combinedFilter.endDates.push(filter.text);
                    break;
                case resStatusFilter:
                    combinedFilter.resvStates.push(filter.text);
                    break;
                case provStatusFilter:
                    combinedFilter.provStates.push(filter.text);
                    break;
                case operStatusFilter:
                    combinedFilter.operStates.push(filter.text);
                    break;
            }
        }
        return combinedFilter;
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
        this.setState({newFilter: {id: this.state.newFilter.id, text: "", type: type.value}, updateHeatMap: false});
    }

    handleAddFilter(){
        let filter = $.extend(true, {}, this.state.newFilter);
        if(filter.text == "" || filter.type == bandwidthFilter && isNaN(filter.text)) {
            console.log("Invalid filter input: must not be empty, and must not have non numeric characters for bandwidth.")
        }
        else{
            this.state.filters.push(filter);
            this.setState({newFilter: {id: filter.id + 1, text: "", type: filter.type}, updateHeatMap: false});
        }
    }

    handleDeleteFilter(filter){
        let id = filter.id;
        let filteredList = this.state.filters.filter((f) => {return f.id != id});
        this.setState({filters: filteredList, updateHeatMap: false});
    }

    handleFilterTextChange(event){
        let newText = event.target.value;
        this.setState({newFilter: {id: this.state.newFilter.id, text: newText, type: this.state.newFilter.type}, updateHeatMap: false});
    }

    handleFilterDateChange(newMoment){
        let newDate = newMoment.toDate();
        this.setState({newFilter: {id: this.state.newFilter.id, text: newDate, type: this.state.newFilter.type}, updateHeatMap: false});
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
                             handleFilterTextChange={this.handleFilterTextChange}
                             handleFilterDateChange={this.handleFilterDateChange}
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
        let input = this.props.newFilter.type == startFilter || this.props.newFilter.type == endFilter ?
            <DateTime
                value={this.props.newFilter.text}
                onChange={this.props.handleFilterDateChange}/> :
            <input style={{ width: "14.25em" }}
                   className="form-control input-md"
                   value={this.props.newFilter.text}
                   onChange={this.props.handleFilterTextChange}/>;

        return(
            <div>
                <p>Filter Reservations By: </p>
                <div style={{ display: "flex", flexWrap: "wrap"}}>
                    <Dropdown options={this.props.filterTypes}
                              value={this.props.newFilter.type}
                              placeholder="Select a filter type"
                              onChange={this.props.handleFilterTypeSelect}
                    />
                    {input}
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
                <ul style={{ display: "flex" , flexDirection: "row", listStyle: "none", flexWrap: "wrap"}}>{listItems}</ul>
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
                style={{marginRight: "10px", marginTop: "5px", backgroundColor: "lightBlue"}}
            >{this.props.filter.type + ": " + this.props.filter.text}</li>
        );
    }
}


module.exports = ReservationListApp;