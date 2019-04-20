import {Actions} from "../Constants"

export const startSearch = () => ({
  type: Actions.START_SEARCH
})

export const setResults = (data) => {
  console.log("called")
  return {
    type: Actions.SET_RESULTS,
    data: data
  }
}