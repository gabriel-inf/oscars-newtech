/*
reservation = {
    junctions: {},
    pipes: {},
    startAt: Date(),
    endAt: Date(),
    description: "",
    connectionId: ""
};
Junction: {id: ~~, label: ~~, fixtures: {}}
fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
Pipe: {id: ~~, from: ~~, to: ~~, bw: ~~}
*/


function validateReservation(reservation){
    let junctions = reservation.junctions;
    let startAt = reservation.startAt;
    let endAt = reservation.endAt;
    let description = reservation.description;

    let errorMessages = [];

    let descriptionValid = description.length > 0;
    if(!descriptionValid){
        errorMessages.push("Description must be provided.");
    }
    let datesValid = startAt < endAt;
    if(!datesValid){
        errorMessages.push("Start date/time must be before end date/time.");
    }

    let junctionStatus = validateJunctions(junctions);
    errorMessages = errorMessages.concat(junctionStatus.errorMessages);

    return {isValid: junctionStatus.isValid && datesValid && descriptionValid, errorMessages: errorMessages};
}

function validateJunctions(junctions){
    let totalValid = 0;
    let totalValidFixtures = 0;
    let junctionNameList = Object.keys(junctions);
    let errorMessages = [];

    for(let index = 0; index < junctionNameList.length; index++){
        let junction = junctions[junctionNameList[index]];
        let numValidFixtures = countValidFixtures(junction);
        if(numValidFixtures > 0){
            totalValidFixtures += numValidFixtures;
        }
        if(junction.id != "--" && junction.id != ""){
            totalValid++;
        }
    }

    if(totalValid != junctionNameList.length || junctionNameList.length == 0){
        errorMessages.push("Make sure that at least one junction has been selected.");
    }

    if(totalValidFixtures < 2){
        errorMessages.push("There must be at least two end points (fixtures) with bandwidth >= 0 across reservation.");
    }

    return {isValid: totalValid > 0 && totalValid == junctionNameList.length && totalValidFixtures > 1, errorMessages: errorMessages};
}

function countValidFixtures(junction){
    let numValidSelectedFixtures = 0;
    let fixtures = junction.fixtures;
    let fixtureNameList = Object.keys(fixtures);
    for(let index = 0; index < fixtureNameList.length; index++){
        let fixture = fixtures[fixtureNameList[index]];
        if(validateFixture(fixture)){
            numValidSelectedFixtures++;
        }
    }
    return numValidSelectedFixtures;
}

// fixture: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}
function validateFixture(fixture){
    return fixture.selected && fixture.bw > -1 && fixture.vlan.length > 0;
}

module.exports = {validateReservation};