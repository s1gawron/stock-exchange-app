const DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

function getTopbar() {
    return `
        <div id="topbar">
          <div id="timeWrapper">
            <div id="welcome">Good to see you!</div>
            <div id="time"></div>
          </div>
    
          <div id="userWrapper">
            <i class="icon-user"></i>
            <div id="userLogin"></div>
          </div>
        </div>
    `;
}

function getClock() {
    const today = new Date();
    const dayName = DAYS[today.getDay()];

    let day = today.getDate();
    if (day < 10) {
        day = `0${day}`;
    }

    const monthName = MONTHS[today.getMonth()];
    const year = today.getFullYear();

    let hour = today.getHours();
    if (hour < 10) {
        hour = `0${hour}`;
    }

    let minutes = today.getMinutes();
    if (minutes < 10) {
        minutes = `0${minutes}`;
    }

    let seconds = today.getSeconds();
    if (seconds < 10) {
        seconds = `0${seconds}`;
    }

    return `Today is: ${dayName}, ${day} ${monthName} ${year} ${hour}:${minutes}:${seconds}`;
}

function getUserLogin() {
    if (localStorage.getItem('token') != null) {
        let username = localStorage.getItem('username');
        return `
            <div id="signOut">
                <p>${username}</p>
                <button id="signOutBtn" onclick="signOut()">Sign out</button>
            </div>
        `;
    }

    return `
        <div>
            <a href="/user/login" id="signIn">Sign in</a>
        </div>
    `;
}

function signOut() {
    localStorage.clear();
    window.location = '/user/login';
}

document.querySelector('header').innerHTML = getTopbar();
setInterval(() => document.querySelector('#time').innerHTML = getClock(), 1000);
document.querySelector('#userLogin').innerHTML = getUserLogin();