import React, { Component } from 'react'
import styled from 'styled-components'
import ResultsData from './../FakeData/FakeResults'
import SearchResult from "./SearchResult"

const Wrapper = styled.div`
  width: 55vw;
  margin-left: 5vw;
`
const SearchSummary = styled.div`
  margin: 20px 12px;
`

export default class SearchResultsPanel extends Component {
  render() {
    return (
      <Wrapper>
        <SearchSummary>About 3000 results in 200ms</SearchSummary>
        {ResultsData.map((result, index) => (
          <SearchResult result={result}></SearchResult>
        ))}
      </Wrapper>
    )
  }
}
