function counter() {
    let today = new Date();

    let days = ["Niedziela", "Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota"];
    let dayName = days[today.getDay()];
    let day = today.getDate();
    if(day < 10) {
        day = "0" + day;
    }

    let months = ["styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec", "lipiec", "sierpień", "wrzesień", "pażdziernik", "listopad", "grudzień"];
    let monthName = months[today.getMonth()];
    let year = today.getFullYear();

    let hour = today.getHours();
    if(hour < 10) {
        hour = "0" + hour;
    }

    let minutes = today.getMinutes();
    if(minutes < 10) {
        minutes = "0" + minutes;
    }

    let seconds = today.getSeconds();
    if(seconds < 10) {
        seconds = "0" + seconds;
    }

    document.getElementById("time").innerHTML = "Dzień dobry!" + "<br/>" + "Dzisiaj jest: " + dayName + ", " + day + " " + monthName + " " + year + " " + hour + ":" + minutes + ":" + seconds;

    setTimeout("counter()", 1000);
}