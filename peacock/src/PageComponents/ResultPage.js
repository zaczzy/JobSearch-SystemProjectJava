import React, { Component } from 'react'
import './ResultPage.css'
import Navbar from './../Components/Navbar'
import SearchResultsPanel from './../Components/SearchResultsPanel'
import WebResultsPanel from './../Components/WebResultsPanel'
import styled from 'styled-components'
import queryString from 'query-string'
import { connect } from 'react-redux';
import { setResults } from '../Redux/Actions'

const Wrapper = styled.div`
  display: flex;
`
class ResultPage extends Component {

  componentDidMount() {
    const values = queryString.parse(this.props.location.search)
    if (this.props.webResultData.length === 0) {
      fetch('http://localhost:8089/fake?query=' + values.query)
      .then(function(response) {
        return response.json();
      })
      .then(function(json) {
        this.props.dispatch(setResults(json));
      }.bind(this));
      
    }
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

const mapStateToProps = state => ({
  webResultData: state.web.WEB_RESULT_DATA
})

export default connect(mapStateToProps)(ResultPage)
