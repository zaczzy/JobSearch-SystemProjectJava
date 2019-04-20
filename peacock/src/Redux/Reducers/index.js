import { combineReducers } from 'redux'
import { connectRouter } from 'connected-react-router'
import Results from './results'

export default (history) => combineReducers({
  router: connectRouter(history),
  results: Results
})
