import {Actions} from "../Constants"

const defaultState = {
  IS_LOADING : true,
  SEARCH_RESULTS : [{}, {}, {}]
}

const ResultsReducer = (state = defaultState, action) => {
  switch (action.type) {
    case Actions.START_SEARCH:
      return {
        ...state,
        IS_LOADING : true,
        SEARCH_RESULTS : [{}, {}, {}]
      }
    case Actions.SET_RESULTS:
      return {
        ...state,
        IS_LOADING : false,
        SEARCH_RESULTS : action.data
      }
    default:
      return state;
  }
}

export default ResultsReducer