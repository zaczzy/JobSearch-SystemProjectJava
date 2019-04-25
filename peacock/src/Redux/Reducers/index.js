import { combineReducers } from 'redux'
import { connectRouter } from 'connected-react-router'
import Results from './results'
import WebResults from './web'

export default (history) => combineReducers({
  router: connectRouter(history),
  results: Results,
  web: WebResults
})
