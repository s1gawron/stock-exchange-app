const DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

function getTopbar() {
    return `
        <div id="topbar">
          <div id="timeWrapper">
            <div id="greeting"></div>
            <div id="time"></div>
          </div>
    
          <div id="userWrapper">
            <i class="icon-user"></i>
            <div id="userLogin"></div>
          </div>
          
          <div id="signOutPlaceholder"></div>
        </div>
    `;
}

function getGreeting() {
    const hour = new Date().getHours();

    if (hour < 12) {
        return 'Good Morning!';
    }

    if (hour < 18) {
        return 'Good Afternoon!';
    }

    return 'Good Evening!';
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
                <p id="usernameDisplay">Zalogowano jako: ${username}</p>
            </div>
        `;
    }

    return `
        <div class="userSign">
            <a href="/user/login" class="userLink">Sign in</a>
        </div>
        <div class="userSign">
            <a href="/user/register" class="userLink">Sign up</a>
        </div>
    `;
}

function getSignOutPlaceholder() {
    if (localStorage.getItem('token') != null) {
        return `
            <button id="signOutBtn" onclick="signOut()">Sign out</button>
        `;
    }

    return ``;
}

function signOut() {
    localStorage.clear();
    window.location = '/user/login';
}

function addComponentCssToFile() {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.type = 'text/css';
    link.href = 'components/topbar/topbar.css';

    document.head.appendChild(link);
}

document.querySelector('header').innerHTML = getTopbar();
document.querySelector('#greeting').innerHTML = getGreeting();
setInterval(() => document.querySelector('#time').innerHTML = getClock(), 1000);
document.querySelector('#userLogin').innerHTML = getUserLogin();
document.querySelector('#signOutPlaceholder').innerHTML = getSignOutPlaceholder();
addComponentCssToFile();