import React, { Component } from 'react'
import styled from 'styled-components'
import SearchBar from './SearchBar'
import { Link } from 'react-router-dom'

const NavWrapper = styled.div`
  width: 100vw;
  height: 60px;
  background: white;
  box-shadow: 0 2px 2px 0 rgba(0,0,0,0.14), 0 3px 1px -2px rgba(0,0,0,0.12), 0 1px 5px 0 rgba(0,0,0,0.20);
  display: flex;
  align-items: center;
  background-color: #fafafa;
  position: -webkit-sticky; /* Safari */
  position: sticky;
  top: 0;
`

const Title = styled.h1`
  font-size: 24px;
  font-weight: 600;
  background: -webkit-linear-gradient(45deg, #09009f, #00ff95);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0px 30px;
  margin-bottom: 3px;
`


export default class Navbar extends Component {
  render() {
    return ( 
      <NavWrapper>
        <Link to="/"><Title>Askme.fyi</Title></Link>
        <SearchBar width="35vw"></SearchBar>
      </NavWrapper>
    )
  }
}
