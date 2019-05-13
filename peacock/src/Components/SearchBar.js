import React, { Component } from 'react'
import { Input, AutoComplete } from 'antd';
import styled from 'styled-components'
import { connect } from 'react-redux'
import { startSearch, setResults, startWebSearch, setWebResults } from '../Redux/Actions'

import { push } from 'connected-react-router'
import { ResultType } from './../Redux/Constants'

const Search = Input.Search;

const AutoCompleteWrapper = styled(AutoComplete)`
  min-width: 300px;
  line-height: 20px;
  .ant-input {
    font-size: 16px;
    border-radius: 30px;
    height: 60px;
    line-height: 18px;
  }
`

class SearchBar extends Component {

  /* TODO: state loading from API call */
  state = {
    dataSource: [],
  }

  handleSearch = (value) => {
    if (value === null || value === "") {
      this.setState({dataSource: []});
      return;
    }
    fetch('http://localhost:8083/h/' + value)
      .then(function(res) {
        try {
          let result = res.json();
          return result;
        } catch (e) {
          console.log(e);
          return [];
        }
      })
      .then(function(json) {
        this.setState({dataSource: json});
      }.bind(this))
  }

  search = (value) => {
    this.props.dispatch(push('/search?query=' + value))
    this.props.dispatch(startSearch())
    this.props.dispatch(startWebSearch())
    /* Query Dolphin Engine */
    fetch('http://68.183.109.201/advanced?query=' + value)
      .then(function(response) {
        return response.json();
      })
      .then(function(json) {
        this.props.dispatch(setResults(json));
      }.bind(this));
      
    fetch('http://localhost:8083/h/' + value, {method: 'POST'})
      .then(function(response) {
        return response.json();

      })
      .then(function(json) {
        console.log(json)
      })

    fetch('http://localhost:8083/?q=' + value)
      .then(function(response) {
        return response.json();
      })
      .then(function(json) {
        if (json.service_type === "weather") {
          console.log(json)
          this.props.dispatch(setWebResults(ResultType.WEATHER_TYPE, json.data)); 
        } else if (json.service_type === "walmart") {
          this.props.dispatch(setWebResults(ResultType.SHOPPING_TYPE, json.data)); 
        }
      }.bind(this));
  }

  render() {
    const { dataSource } = this.state;

    return (
      <div className="certain-category-search-wrapper">
        <AutoCompleteWrapper
          className="certain-category-search"
          dropdownClassName="certain-category-search-dropdown"
          dropdownMatchSelectWidth={false}
          size="large"
          style={{width: this.props.width}}
          dataSource={dataSource}
          onSearch={this.handleSearch}
          placeholder="Ask Me Anything.."
        >
          <Search onSearch={value => this.search(value)} />
        </AutoCompleteWrapper>
      </div>
    )
  }
}

export default connect()(SearchBar)
