import React, { Component } from 'react'
import { Input, AutoComplete } from 'antd';
import styled from 'styled-components'

const Search = Input.Search;

const AutoCompleteWrapper = styled(AutoComplete)`
  width: 50vw;
  min-width: 500px;
  height: 60px;
  line-height: 20px;
  .ant-input {
    font-size: 16px;
    border-radius: 30px;
    height: 60px;
    line-height: 18px;
  }
`

export default class SearchBar extends Component {

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

  render() {
    const { dataSource } = this.state;

    /* TODO: change onSearch Method */
    return (
      <div className="certain-category-search-wrapper">
        <AutoCompleteWrapper
          className="certain-category-search"
          dropdownClassName="certain-category-search-dropdown"
          dropdownMatchSelectWidth={false}
          size="large"
          dataSource={dataSource}
          onSearch={this.handleSearch}
          placeholder="Ask Me Anything.."
        >
          <Search onSearch={value => console.log(value)} />
        </AutoCompleteWrapper>
      </div>
    )
  }
}
