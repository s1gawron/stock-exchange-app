import React from 'react';
import './App.css';
import {BrowserRouter as Router, Route, Routes} from "react-router-dom"
import HomePage from "./page/home/HomePage";
import RegisterPage from "./page/register/RegisterPage";

export default function App(): React.ReactElement {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage/>}/>
                <Route path="/user/register" element={<RegisterPage/>}/>
            </Routes>
        </Router>
    );
}
