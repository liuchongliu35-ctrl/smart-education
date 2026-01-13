import { Avatar, Button, ConfigProvider, Empty, Input, Tag,Typography } from "antd"
import { useState, useEffect } from 'react'
import style from '@/pages/TextEditor/textEditor.module.css'
import {ReloadOutlined,SendOutlined} from '@ant-design/icons'
import ModalLoadingComponent from "../Loading/modalLoading"
import NoneDataIcon from '@/assets/svg/暂无内容.svg'
import ChatResponse from "./chatResponse"
import { postWordModify } from "@/apis/preparation"
import { ping } from 'ldrs'
ping.register()

const typeList = [
    {key:1,text:'结构优化'},
    {key:2,text:'文本编写'},
    {key:3,text:'内容续写'},
]
const { TextArea } = Input

const WordComponent = ({modifyContent,tdId}) =>{

   useEffect(() => {
   setText(modifyContent)
   setSaveModify(modifyContent)
 }, [modifyContent])
    
    const [currentState,setCurrentState] = useState('0')
    const [textType,setTextType] = useState(1)
    const [text,setText] = useState('')
    const [improveText,setImproveText] = useState('')
    const [isLoading,setIsLoading] = useState(false)
    const [saveModify,setSaveModify] = useState('')

    function typeSelect (e){
        switch (e) {
            case 1:
                setTextType(1)
                break;
            case 2:
                setTextType(2)
                break;
            case 3:
                setTextType(3)
                break;
        
            default:
                break;
        }
    }
    //文案优化标题渲染
    function typeRender (){
        if(currentState !== '0'){
            switch (textType) {
                case 1:
                    return(<span>结构优化-<span>Structure Design</span></span>)
                case 2:
                    return(<span>文案编写-<span>Writing Copy</span></span>)
                case 3:
                    return(<span>内容续写-<span>Content Continuation</span></span>)
                default:
                    break;
            }
        } else {
            return('文案优化')
        }
    }

    function changeCurrentState(e){
        setCurrentState(e)
    }

    const modifyWord = async() =>{
        setIsLoading(true)
        changeCurrentState('0')
        const prompt = {
            other:"",
            select:textType,
            tdId:tdId,
            text:text
        }
        let {data} = await postWordModify(prompt)
        setImproveText(data)
        setTimeout(() => {
            setIsLoading(false)
            setText('')
            changeCurrentState('1')
        }, 50);
    }

    const againGenerate = async() =>{
        setIsLoading(true)
        changeCurrentState('0')
        const prompt = {
            other:"",
            select:textType,
            tdId:tdId,
            text:saveModify
        }
        let {data} = await postWordModify(prompt)
        setImproveText(data)
        setTimeout(() => {
            setIsLoading(false)
            setText('')
            changeCurrentState('1')
        }, 50);
    }


    return(
        <ConfigProvider>   
           <div 
           
           style={{width:'100%',height:40,fontFamily:'siyuan',lineHeight:'40px',paddingInline:20,paddingBottom:10,borderBottom:'1px solid #eaeaea',fontSize:13.5}}>
            {typeRender()}
                {/* <Button onClick={()=>changeCurrentState('1')}>2</Button>
                <Button onClick={()=>changeCurrentState('0')}>3</Button> */}
            </div>
         {currentState === '0' ?  
            <div style={{width:'100%',flex:1,display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center'}}>         
             <Avatar src={NoneDataIcon} size={128} />
            <div style={{fontFamily:'youshe',color:'#000',fontSize:22}}>暂无内容</div>
            </div> : <ChatResponse onChange={(e)=>changeCurrentState(e)} content={improveText} />}
            {isLoading &&    
            <div style={{width:'100%',height:450,background:'#fff',display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center',position:'absolute',top:40}}>
                <ModalLoadingComponent />
            </div>}
            <div className={style.generateInput}>
                <div style={{width:'100%',marginBottom:5,fontFamily:'siyuan',display:'flex',alignItems:'center',justifyContent:'center',color:'#293788'}}>
                {currentState === '1' &&  <span style={{cursor:'pointer'}}>
                <l-ping size="18" speed="5" color="black" ></l-ping>
                停止  </span>}
                {currentState === '2' &&  <span style={{cursor:'pointer'}} onClick={()=>againGenerate()}><ReloadOutlined style={{marginRight:4}} />重新生成 </span>}
                
                </div>

                <TextArea style={{height:130}} 
                placeholder="请输入文案. . ." 
                value={text} 
                onChange={(e)=>setText(e.target.value)} 
                onPressEnter={()=>modifyWord()}
                />

                <div style={{display:'flex',justifyContent:'space-between',width:'80%',marginTop:10}}>
                {typeList.map((item)=>(
                    <div 
                    className={textType === item.key ? style.focusTag : style.defTag}
                    key={item.key} 
                    onClick={()=>typeSelect(item.key)}>{item.text}</div>
                ))}
                </div>
            </div>
        </ConfigProvider>
    )
}

export default WordComponent