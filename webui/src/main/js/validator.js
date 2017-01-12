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

    let junctionsValid = validateJunctions(junctions);
    let datesValid = startAt < endAt;
    let descriptionValid = description.length > 0;

    return junctionsValid && datesValid && descriptionValid;
}

function validateJunctions(junctions){
    let totalValid = 0;
    let totalValidFixtures = 0;
    let junctionNameList = Object.keys(junctions);
    for(let index = 0; index < junctionNameList.length; index++){
        let junction = junctions[junctionNameList[index]];
        let numValidFixtures = countValidFixtures(junction);
        if(numValidFixtures > 0){
            totalValid++;
            totalValidFixtures += numValidFixtures;
        }
    }
    return totalValid > 0 && totalValid == junctionNameList.length && totalValidFixtures > 1;
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
    return fixture.selected && fixture.bw > 0 && fixture.vlan.length > 0;
}

module.exports = {validateReservation};