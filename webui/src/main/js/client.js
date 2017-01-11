'use strict';

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
        let params = opts.params;
        if(params){
            if(typeof params === 'object'){
                params = Object.keys(params).map(function (key){
                    return encodeURIComponent(key) + "=" + encodeURIComponent(params[key]);
                }).join('&');
            }
            xhr.send(params);
        }
        else{
            xhr.send();
        }
    });
}

module.exports = {loadJSON};