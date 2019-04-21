import React from 'react'
import styled from 'styled-components'
import { Skeleton } from 'antd';

const Card = styled.div`
  width: 50vw;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 2px 0 rgba(0,0,0,0.10), 0 3px 1px -2px rgba(0,0,0,0.07), 0 1px 5px 0 rgba(0,0,0,0.07);
  :hover {
    box-shadow: 0 12px 17px 2px rgba(0,0,0,0.06), 0 5px 22px 4px rgba(0,0,0,0.04), 0 7px 8px -4px rgba(0,0,0,0.04);
  }
  padding: 15px 20px;
  margin: 15px;
`

const TitleLink = styled.div`
  a {
    font-size: 20px;
    color: #09009f;
  }
  a:hover {
    text-decoration: underline;
  }
  a:visited {
    color: #430297;
  }
  white-space: nowrap;
  height: 2.2em;
  overflow: hidden;
  text-overflow: ellipsis;
`

const UrlText = styled.div`
  color: #006621;
`

const Excerpt = styled.div`
  color: #666666;
`

const SearchResult = ({result, isLoading}) => (
  <Card>
    {isLoading && <Skeleton active />}
    {!isLoading && <div>
      <TitleLink><a href={result.url}>{result.title}</a></TitleLink>
      <UrlText>{result.url}</UrlText>
      <Excerpt>{result.exerpt}</Excerpt>
      </div> }
  </Card>
)

export default SearchResult
