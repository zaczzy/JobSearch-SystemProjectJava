import React, { Component } from 'react'
import './HomePage.css'
import 'antd/dist/antd.css'
import styled from 'styled-components'
import SearchBar from './../Components/SearchBar'

const Title = styled.h1`
  font-size: 72px;
  background: -webkit-linear-gradient(45deg, #09009f, #00ff95);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
`

const FootNote = styled.h6`
  margin-top: 40px;
  font-size: 16px;
  color: #cccccc;
`

export default class HomePage extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <Title>Askme.fyi</Title>
          <SearchBar width="50vw"></SearchBar>
          <FootNote>&copy; A CIS 555 Demo, Han Yan, Yujiang Duan, Zeyu Zhao, Zhilei Zheng, 2019</FootNote>
        </header>
      </div>
    )
  }
}
