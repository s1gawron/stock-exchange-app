function showHide() {
    let date = new Date();
    let dayOfWeek = date.getDay();
    let hourOfDay = date.getHours();

    if (dayOfWeek === 0 || dayOfWeek === 6 || hourOfDay > 17 || hourOfDay < 11) {
        $("#buttonWrapper a").click(function (e) {
            e.preventDefault();
        });
    }
}

$(document).ready(function () {
    showHide();
});