import {Actions, ResultType} from "../Constants"

const defaultState = {
  WEB_RESULT_TYPE : ResultType.NONE_TYPE,
  WEB_RESULT_DATA: []
}

const WebResultReducer = (state = defaultState, action) => {
  switch (action.type) {
    case Actions.START_WEB_SEARCH:
      return {
        ...state,
        WEB_RESULT_TYPE : ResultType.NONE_TYPE,
        WEB_RESULT_DATA : []
      }
    case Actions.SET_WEB_RESULT:
      return {
        ...state,
        WEB_RESULT_TYPE : action.resultType,
        WEB_RESULT_DATA : action.resultData
      }
    default:
      return state;
  }
}

export default WebResultReducer