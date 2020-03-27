function getUrlVars() {
    let vars = {};
    let parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

function changeSelectOptions() {
    let stockTicker = getUrlVars()["ticker"];
    let action = getUrlVars()["action"];

    $('#pickStock').val(stockTicker);
    $('#pickAction').val(action);
}

$(document).ready(function () {
    changeSelectOptions();
});