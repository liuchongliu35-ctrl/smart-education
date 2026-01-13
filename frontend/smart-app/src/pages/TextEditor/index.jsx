import TextEditor from "../../component/Editor/TextEditor"
// import style from './textEditor.module.css'
import React, { useEffect, useState } from 'react';

import {Skeleton} from 'antd'


const TextEditorLoading = () => {

    return(
        <>
        <div style={{display:"flex"}}>
        <div style={{width:'10%'}}></div>
        <div style={{width:'80%'}}>
             <Skeleton active />
                  <br />
                  <Skeleton active />
        </div>
        <div style={{width:'10%'}}></div>
        </div>
        </>
    )
  }



const TextEditorPage = () => {
    const [isLoading,setIsLoading] = useState(true)

    useEffect(() => {
    setTimeout(() => {
      setIsLoading(false)
    }, 500)
    }, [])

      
    return(
      <>
        <TextEditor />
      </>
      
       
    )
  }




  export default TextEditorPage