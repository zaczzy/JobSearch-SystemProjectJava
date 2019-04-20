import React, { Component } from 'react';
import HomePage from './PageComponents/HomePage'
import ResultPage from './PageComponents/ResultPage'

import { Provider } from 'react-redux'
import { Route, Switch } from 'react-router'
import { ConnectedRouter } from 'connected-react-router'
import configureStore, { history } from './Redux'

const store = configureStore()

const App = () => (
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <Switch>
        <Route exact path="/" component={HomePage} />
        <Route path="/search" component={ResultPage} />
      </Switch>
    </ConnectedRouter>
  </Provider>
);

export default App;
