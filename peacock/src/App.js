import React, { Component } from 'react';
import HomePage from './PageComponents/HomePage'
import ResultPage from './PageComponents/ResultPage'
import { BrowserRouter as Router, Route, Link } from "react-router-dom";

class App extends Component {
  render() {
    return (
      <Router>
        <Route exact path="/" component={HomePage} />
        <Route path="/search" component={ResultPage} />
      </Router>
    );
  }
}

export default App;
