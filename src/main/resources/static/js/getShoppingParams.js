function getUrlVars() {
    let vars = {};
    let parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, value) {
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

function changeGoBackLink() {
    let prevSite = getUrlVars()["prev"];

    if (prevSite === "myWallet") {
        $("a[href='statsWIG20']").attr('href', 'myWallet')
    }
}

$(document).ready(function () {
    changeSelectOptions();
    changeGoBackLink();
});