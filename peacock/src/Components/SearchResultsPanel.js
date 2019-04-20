import React, { Component } from 'react'
import styled from 'styled-components'
import ResultsData from './../FakeData/FakeResults'
import SearchResult from "./SearchResult"
import { connect } from 'react-redux'

const Wrapper = styled.div`
  width: 55vw;
  margin-left: 5vw;
`
const SearchSummary = styled.div`
  margin: 20px 12px;
`
const SearchResultsPanel = ({results, isLoading}) => (
  <Wrapper>
    { !isLoading && <SearchSummary>About 3000 results in 200ms</SearchSummary> }
    {results.map((result, index) => (
      <SearchResult result={result} isLoading={isLoading}></SearchResult>
    ))}
  </Wrapper>
)

const mapStateToProps = state => ({
  results: state.results.SEARCH_RESULTS,
  isLoading: state.results.IS_LOADING
})

export default connect(mapStateToProps)(SearchResultsPanel)