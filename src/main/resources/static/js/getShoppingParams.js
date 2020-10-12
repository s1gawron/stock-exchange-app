function getUrlVars() {
    let vars = {};
    let parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, value) {
        vars[key] = value;
    });
    return vars;
}

function getPathVariable() {
    return window.location.pathname.split('/');
}

function changeSelectOptions() {
    let stockTicker = getPathVariable()[3];
    let action = getPathVariable()[4];

    $('#pickStock').val(stockTicker.toUpperCase());
    $('#pickAction').val(action);
}

$(document).ready(function () {
    changeSelectOptions();
});