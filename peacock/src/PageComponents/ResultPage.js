import React, { Component } from 'react'
import './ResultPage.css'
import Navbar from './../Components/Navbar'
import SearchResultsPanel from './../Components/SearchResultsPanel'
import WebResultsPanel from './../Components/WebResultsPanel'
import styled from 'styled-components'

const Wrapper = styled.div`
  display: flex;
`

export default class ResultPage extends Component {
  render() {
    return (
      <div>
        <Navbar></Navbar>
        <Wrapper>
          <SearchResultsPanel></SearchResultsPanel>
          <WebResultsPanel></WebResultsPanel>
        </Wrapper>
      </div>
    )
  }
}
