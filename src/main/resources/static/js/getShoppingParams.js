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
    let action = getPathVariable()[2];

    $('#pickStock').val(stockTicker);
    $('#pickAction').val(action);
}

//TO DO
function changeGoBackLink() {
    let prevSite = getUrlVars()["prev"];

    if (prevSite === "myWallet") {
        $("a[href='statsWIG20']").attr('href', 'stock/myWallet')
    }
}

$(document).ready(function () {
    changeSelectOptions();
    changeGoBackLink();
});