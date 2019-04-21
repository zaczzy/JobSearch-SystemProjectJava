import {Actions} from "../Constants"

export const startSearch = () => ({
  type: Actions.START_SEARCH
})

export const setResults = (data) => {
  return {
    type: Actions.SET_RESULTS,
    data: data
  }
}

export const startWebSearch = () => ({
  type: Actions.START_WEB_SEARCH
})

export const setWebResults = (type, data) => {
  return {
    type: Actions.SET_WEB_RESULT,
    resultType: type,
    resultData: data,
  }
}