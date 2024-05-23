import React, {ReactElement} from 'react';
import './App.css';
import {BrowserRouter as Router, Route, Routes} from "react-router-dom"
import MainPage from "./page/MainPage";

function App(): ReactElement {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage/>}/>
            </Routes>
        </Router>
    );
}

export default App;
