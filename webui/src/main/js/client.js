'use strict';
const moment = require('moment');
/*
 opts = {
    method: String,
    url: String,
    params: String | Object,
    headers: Object
 }
 */
function loadJSON(opts) {
    return new Promise(function(resolve, reject){
        let xhr = new XMLHttpRequest();
        xhr.overrideMimeType('application/json');
        xhr.open(opts.method, opts.url);
        xhr.onload = function(){
            if (this.status >= 200 && this.status < 300) {
                resolve(xhr.response);
            } else {
                reject({
                    status: this.status,
                    statusText: xhr.statusText
                });
            }
        };
        xhr.onerror = function () {
            reject({
                status: this.status,
                statusText: xhr.statusText
            });
        };
        if(opts.headers){
            Object.keys(opts.headers).forEach(function (key){
                xhr.setRequestHeader(key, opts.headers[key]);
            });
        }
        xhr.setRequestHeader("Content-type", "application/json; charset=UTF-8");
        let params = opts.params;
        if(params){
            if(typeof params === 'object'){
                params = JSON.stringify(params);
                /*params = Object.keys(params).map(function (key){
                    return encodeURIComponent(key) + "=" + encodeURIComponent(params[key]);
                }).join('&');*/
            }
            xhr.send(params);
        }
        else{
            xhr.send();
        }
    });
}

function submitReservation(url, reservation){
    // Must convert start/end dates to Integers
    let modifiedRes = formatReservation(reservation);
    return submit("POST", url, modifiedRes)
}

function submit(method, url, payload){
    let csrfToken = $("meta[name='_csrf']").attr("content");
    let csrfHeader = $("meta[name='_csrf_header']").attr("content");

    let headers = {};
    headers[csrfHeader] = csrfToken;

    return loadJSON({method: method, url: url, headers: headers, params: payload});
}

function formatReservation(reservation){
    return {
        junctions: reservation.junctions,
        pipes: reservation.pipes,
        startAt: parseInt(moment(reservation.startAt).unix()),
        endAt: parseInt(moment(reservation.endAt).unix()),
        description: reservation.description,
        connectionId: reservation.connectionId
    };
}

module.exports = {loadJSON, submitReservation, submit};