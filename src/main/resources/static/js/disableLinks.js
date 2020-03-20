function showHide() {
    let date = new Date();
    let dayOfWeek = date.getDay();
    let hourOfDay = date.getHours();

    if (dayOfWeek === 6 || dayOfWeek === 0 || hourOfDay < 9 || hourOfDay >= 17) {
        $(".buttonWrapper").click(function (e) {
            e.preventDefault();
            alert("Gielda czynna od Poniedzialku do Piatku w godzinach od 9 do 17. Zapraszamy!")
        });
    }
}

$(document).ready(function () {
    showHide();
});