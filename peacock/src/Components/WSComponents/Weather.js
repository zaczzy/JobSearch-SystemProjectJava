import React, { Component } from 'react'
import WeatherIcon from 'react-icons-weather';
import styled from 'styled-components'

const Weather = styled.div`
  font-size: 55px;
  margin: 10px;
  margin-bottom: -5px;
  margin-right: 40px;
`

const Wrapper = styled.div`
  display: flex;
  align-items: center;
`

const WeatherString = styled.div`
  font-size: 18px;
  font-weight: 400;
`

const CityString = styled.div`
  font-size: 20px;
  font-weight: 600;
`

const kToC = (k) => {
  return (k - 273.15).toFixed(2)
}

const getDateString = (m) => {
  const date = new Date(m);
  return date.toDateString
}

const WeatherComp = ({data}) => {
  return (
    <Wrapper>
      <Weather>
        <WeatherIcon name="owm" iconId={data.icon_id} flip="horizontal" rotate="90" />
      </Weather>
      <div>
        <CityString>{data.city}</CityString>
        <WeatherString>{data.weather} &nbsp;|&nbsp; {kToC(data.temp)}°C</WeatherString>
        ({data.dt})
      </div>
    </Wrapper>
  )
}

export default WeatherComp