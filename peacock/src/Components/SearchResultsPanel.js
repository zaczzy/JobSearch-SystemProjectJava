import React from 'react'
import styled from 'styled-components'
import SearchResult from "./SearchResult"
import { connect } from 'react-redux'

const Wrapper = styled.div`
  width: 55vw;
  margin-left: 5vw;
`
const SearchSummary = styled.div`
  margin: 20px 12px;
`
const SearchResultsPanel = ({results, isLoading, numResults, timeToResult}) => (
  <Wrapper>
    { !isLoading && <SearchSummary>About {numResults} results in {(timeToResult).toFixed(2) / 1000} seconds</SearchSummary> }
    {results.map((result, index) => (
      <SearchResult result={result} isLoading={isLoading} key={index}></SearchResult>
    ))}
  </Wrapper>
)

const mapStateToProps = state => ({
  results: state.results.SEARCH_RESULTS,
  isLoading: state.results.IS_LOADING,
  numResults: state.results.NUM_RESULTS,
  timeToResult: state.results.TIME_TO_RESULT
})

export default connect(mapStateToProps)(SearchResultsPanel)