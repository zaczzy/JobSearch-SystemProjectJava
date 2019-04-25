import React, { Component } from 'react'
import styled from 'styled-components'
import { Card } from 'antd';
const { Meta } = Card;

const Wrapper = styled.div`
  overflow-x: scroll;
`

const ProductTitle = styled.div`
  font-size: 14px;
  color: black;
  display: block; /* or inline-block */
  text-overflow: ellipsis;
  word-wrap: break-word;
  overflow: hidden;
  max-height: 3em;
  line-height: 1.5em;
`

const SellerTitle = styled.div`
  color: green;
`

const ProductCard = ({data}) => {
  const {seller, productUrl, title, thumbnailUrl, price} = data
  return (
    <a href={productUrl}>
      <Card
        hoverable
        style={{ width: 180, margin: "5px 10px" }}
        cover={<img alt="example" src={thumbnailUrl} />}>
        <ProductTitle>{title}</ProductTitle>
        <SellerTitle>{seller}</SellerTitle>
        <div>{price}</div>
      </Card>
    </a>
  )
}

const ShoppingComp = ({data}) => {
  return (
    <div>
      <h3>Shopping</h3>
      <Wrapper>
        <div style={{width:`${data.length * 200 + 10}px`, display:"flex"}}>
          {data.map((result, index) => (
            <ProductCard data={result} key={index}></ProductCard>
          ))}
        </div>
      </Wrapper>
    </div>
  )
}

export default ShoppingComp

