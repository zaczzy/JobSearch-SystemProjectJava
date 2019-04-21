import React, { Component } from 'react'
import WeatherComp from "./Weather"
import ShoppingComp from "./Shopping"
import { ResultType } from "../../Redux/Constants"
import styled from "styled-components"

const ResultContainer = styled.div`
  background: white;
  box-shadow: 0 2px 2px 0 rgba(0,0,0,0.10), 0 3px 1px -2px rgba(0,0,0,0.07), 0 1px 5px 0 rgba(0,0,0,0.07);
  border-radius: 10px;
  padding: 15px;
  margin: 15px;
`

const EmptyContainer = styled.div`
  visibility: hidden;
`

const WSComponent = ({type, data}) => {
  switch(type) {
    case ResultType.WEATHER_TYPE:
      return <ResultContainer><WeatherComp data={data}/></ResultContainer>
    case ResultType.SHOPPING_TYPE:
      return <ResultContainer><ShoppingComp data={data}/></ResultContainer>
    default:
      return <EmptyContainer></EmptyContainer>
  }
}

export default WSComponent

