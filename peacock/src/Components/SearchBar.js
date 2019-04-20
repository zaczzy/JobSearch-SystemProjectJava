import React, { Component } from 'react'
import { Input, AutoComplete } from 'antd';
import styled from 'styled-components'
import ResultsData from './../FakeData/FakeResults'
import { connect } from 'react-redux'
import { startSearch, setResults } from '../Redux/Actions'

import { push } from 'connected-react-router'

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
    this.setState({
      dataSource: !value ? [] : [
        value,
        value + value,
        value + value + value,
      ],
    });
  }

  search = (value) => {
    console.log("dispatch: " + value);
    console.log(this.props);
    this.props.dispatch(push('/search?query=' + value))
    this.props.dispatch(startSearch())
    const shuffled = ResultsData.sort(() => 0.5 - Math.random());
    let selected = shuffled.slice(0, 15);
    setTimeout(function(props){ props.dispatch(setResults(selected)); }, 900, this.props);
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
