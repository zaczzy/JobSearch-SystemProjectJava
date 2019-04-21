import React, { Component } from 'react'
import './ResultPage.css'
import Navbar from './../Components/Navbar'
import SearchResultsPanel from './../Components/SearchResultsPanel'
import WebResultsPanel from './../Components/WebResultsPanel'
import styled from 'styled-components'
import queryString from 'query-string'
import { connect } from 'react-redux';
import { startSearch, setResults } from '../Redux/Actions'

import ResultsData from './../FakeData/FakeResults'

const Wrapper = styled.div`
  display: flex;
`
class ResultPage extends Component {

  componentDidMount() {
    const values = queryString.parse(this.props.location.search)
    console.log(values.query)
    this.props.dispatch(startSearch())
    const shuffled = ResultsData.sort(() => 0.5 - Math.random());
    let selected = shuffled.slice(0, 15);
    setTimeout(function(props){ props.dispatch(setResults(selected)); }, 300, this.props);
  }

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

export default connect()(ResultPage)
