import React, { Component } from 'react'
import styled from 'styled-components'
import { connect } from 'react-redux'


const Wrapper = styled.div`
  width: 35vw;
  margin-right: 10vw;
`

const WebResultsPanel = ({hasResult, result}) => (
  <Wrapper>
    { hasResult && <div>lmao</div> }
  </Wrapper>
)

const mapStateToProps = state => ({
  hasResult: state.results.HAS_WEB_RESULT,
  result: state.results.WEB_RESULT
})

export default connect(mapStateToProps)(WebResultsPanel)