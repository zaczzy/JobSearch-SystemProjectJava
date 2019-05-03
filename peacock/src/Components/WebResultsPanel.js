import React from 'react'
import styled from 'styled-components'
import { connect } from 'react-redux'
import WSComponent from './WSComponents'

const Wrapper = styled.div`
  width: 35vw;
  margin-right: 10vw;
`

const SearchSummary = styled.div`
  margin: 20px 12px;
`

const WebResultsPanel = ({isLoading, webResultType, webResultData}) => (
  <Wrapper>
    <SearchSummary>&nbsp;</SearchSummary>
    {!isLoading && <WSComponent type={webResultType} data={webResultData}/> }
  </Wrapper>
)

const mapStateToProps = state => ({
  isLoading: state.results.IS_LOADING,
  webResultType: state.web.WEB_RESULT_TYPE,
  webResultData: state.web.WEB_RESULT_DATA
})

export default connect(mapStateToProps)(WebResultsPanel)