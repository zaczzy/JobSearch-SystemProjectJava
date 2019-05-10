import {Actions} from "../Constants"

const defaultState = {
  IS_LOADING : true,
  SEARCH_RESULTS : [{}, {}, {}],
  NUM_RESULTS : 0,
  TIME_TO_RESULT: 0,
  SEARCH_START: 0,
}

const ResultsReducer = (state = defaultState, action) => {
  switch (action.type) {
    case Actions.START_SEARCH:
      return {
        ...state,
        IS_LOADING : true,
        SEARCH_START : (new Date()).getTime(),
        SEARCH_RESULTS : [{}, {}, {}]
      }
    case Actions.SET_RESULTS:
      return {
        ...state,
        IS_LOADING : false,
        SEARCH_RESULTS : action.data,
        NUM_RESULTS : action.data.length,
        TIME_TO_RESULT: (new Date()).getTime() - state.SEARCH_START,
        SEARCH_START: 0
      }
    default:
      return state;
  }
}

export default ResultsReducer