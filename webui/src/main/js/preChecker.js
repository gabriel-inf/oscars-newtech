const client = require('./client');

/*
reservation = {
    junctions: {},
    pipes: {},
    startDate: startDate,
    endDate: endDate,
    description: ""
};
Junction: {id: ~~, label: ~~, fixtures: {}}
fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
Pipe: {id: ~~, from: ~~, to: ~~, bw: ~~}
*/

function preCheck(reservation){
    let response = {};

    return response;
}

function validateReservation(reservation){
    let junctions = reservation.junctions;
    let startAt = reservation.startAt;
    let endAt = reservation.endAt;
    let description = reservation.description;

    let junctionsValid = validateJunctions(junctions);
    let datesValid = startAt < endAt;
    let descriptionValid = description.length > 0;

    return junctionsValid && datesValid && descriptionValid;
}

function validateJunctions(junctions){
    let numValid = 0;
    let junctionNameList = Object.keys(junctions);
    for(let index = 0; index < junctionNameList.length; index++){
        let junction = junctions[junctionNameList[index]];
        if(validateJunction(junction)){
            numValid++;
        }
    }
    return numValid > 0 && numValid == junctionNameList.length;
}

function validateJunction(junction){
    let numValidSelectedFixtures = 0;
    let fixtures = junction.fixtures;
    let fixtureNameList = Object.keys(fixtures);
    for(let index = 0; index < fixtureNameList.length; index++){
        let fixture = fixtures[fixtureNameList[index]];
        if(validateFixture(fixture)){
            numValidSelectedFixtures++;
        }
    }
    return numValidSelectedFixtures > 1;
}

// fixture: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}
function validateFixture(fixture){
    return fixture.selected && fixture.bandwidth > 0 && fixture.vlan.length > 0;
}

module.exports = {validateReservation, preCheck};