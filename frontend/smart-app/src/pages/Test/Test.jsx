import TextEditor from "../../component/Editor/TextEditor"
import style from './test.module.css'
import React, { useState, useRef } from 'react';
import { Popover, Button } from 'antd';

const TestPage = () => {

 

      
    return(
        <>
      <div style={{display:'flex'}}>
      <div style={{marginRight:20,width:50,height:50,background:'#3A6FF7'}}></div>
      <div style={{marginRight:20,width:50,height:50,background:'#00C4B3'}}></div>
      <div style={{marginRight:20,width:50,height:50,background:'#FF6B35'}}></div>
      <div style={{marginRight:20,width:50,height:50,background:'#F5F7FA'}}></div>
      <div style={{marginRight:20,width:50,height:50,background:'#2D3436'}}></div>
      <div style={{width:150,height:50,borderRadius:20,background:'linear-gradient(156.07deg, #5ed1e08f , #e5bbdecc 77.2%)'}}></div>
    </div>
        <h1>测试页面</h1>
        <div> 
        测试页面测试页面测试页面测试页面测试页面测试页面
        </div>
        <hr></hr>
        <div style={{display:"flex"}}>
        <div className={style.left}></div>
        <TextEditor />
        <div className={style.right}></div>
        </div>
        </>
    )
  }




  export default TestPage