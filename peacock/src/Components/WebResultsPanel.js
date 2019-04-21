import React from 'react'
import styled from 'styled-components'
import { connect } from 'react-redux'

const Wrapper = styled.div`
  width: 35vw;
  margin-right: 10vw;
`

const ResultContainer = styled.div`
  background: white;
  box-shadow: 0 2px 2px 0 rgba(0,0,0,0.10), 0 3px 1px -2px rgba(0,0,0,0.07), 0 1px 5px 0 rgba(0,0,0,0.07);
  border-radius: 10px;
  padding: 15px;
  margin: 15px;
`

const SearchSummary = styled.div`
  margin: 20px 12px;
`

const WebResultsPanel = ({hasResult, result, isLoading}) => (
  <Wrapper>
    <SearchSummary>&nbsp;</SearchSummary>
    {!isLoading && <ResultContainer>[Placeholder]</ResultContainer> }
  </Wrapper>
)

const mapStateToProps = state => ({
  hasResult: state.results.HAS_WEB_RESULT,
  result: state.results.WEB_RESULT,
  isLoading: state.results.IS_LOADING
})

export default connect(mapStateToProps)(WebResultsPanel)