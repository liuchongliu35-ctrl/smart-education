import { ConfigProvider, Input,Button, Empty, Typography, Avatar, Image } from "antd"
import React, { useEffect, useState } from 'react';
import Icon, {SearchOutlined} from '@ant-design/icons'
import style from '@/pages/TextEditor/textEditor.module.css'
import ModalLoadingComponent from "../Loading/modalLoading"
import NoneDataIcon from '@/assets/svg/暂无数据.svg'
import { postGenerateVideo } from "../../apis/preparation";
import 'ldrs/ring'
import { spiral } from 'ldrs'
spiral.register()



const VideoComponent = ({modifyContent,tdId}) =>{
    
     useEffect(() => {
     setText(modifyContent)
       
     }, [modifyContent])

    const [isGenerate,setIsGenerate] = useState('0')
    const [text,setText] = useState('')
    const [videoList,setVideoList] = useState([])

    
    const generatePhoto = async() =>{
            setIsGenerate('1')
            const prompt = {
                text:text
            }
            let {data} = await postGenerateVideo(prompt)
            console.log(data)
            setVideoList(data)
            setTimeout(() => {
                setIsGenerate('2')
                setText('')
            }, 50);
        }

    return(
        <ConfigProvider>
            <div style={{width:'90%',margin:'auto',height:'max-content'}}>
            <div style={{width:'100%',height:40,fontFamily:'siyuan',lineHeight:'40px',paddingBottom:10,borderBottom:'1px solid #eaeaea',fontSize:13.5}}>
              视频生成</div>
                <Input 
                prefix={<SearchOutlined />}
                placeholder="请输入关键词. . ." 
                value={text} 
                onChange={(e)=>setText(e.target.value)} 
                onPressEnter={()=>generatePhoto()}
                />
      
            
            </div>
            {isGenerate === '1' && <div style={{flex:1,display:'flex',justifyContent:'center',alignItems:'center'}}>
            <div style={{height:'max-content'}}>
            <ModalLoadingComponent />
            </div>         
         </div> }
            {isGenerate === '0' && 
            <>
    
           
            <div style={{flex:1,display:'flex',justifyContent:'center',alignItems:'center'}}>
            <div style={{width:'100%',flex:1,display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center'}}>         
             <Avatar src={NoneDataIcon} size={128} />
            <div style={{fontFamily:'youshe',color:'#000',fontSize:22}}>暂无内容</div>
            </div>
            
         </div> 
         </>}
            {isGenerate === '2' && <div style={{width:'100%',paddingInline:20,flex:1,display:'flex',flexDirection:'column',alignItems:'center',minHeight:0,overflowY:'auto'}}>             
               {videoList?.map((item,index)=>(
                      <div key={index} style={{width:'100%'}}>
                       <div style={{marginTop:20,marginBottom:10,fontSize:14,fontWeight:600,textAlign:'left',width:'100%'}}>{item.keyword}</div>
                       <div style={{width:250,wordWrap:'break-word',whiteSpace:'normal',overflow:'hidden',marginBottom:20}}>
                       <a href={item.videoUrl[0]}>{item.videoUrl[0]}</a>
                       </div>
                       <div style={{width:250,wordWrap:'break-word',whiteSpace:'normal',overflow:'hidden',marginBottom:20}}>
                       <a href={item.videoUrl[1]}>{item.videoUrl[1]}</a>
                       </div>
                       <div style={{width:250,wordWrap:'break-word',whiteSpace:'normal',overflow:'hidden',marginBottom:20   }}>
                       <a href={item.videoUrl[2]}>{item.videoUrl[2]}</a>
                       </div>
                      </div>
                    ))}
              </div>}
            
 
        </ConfigProvider>
    )
}

export default VideoComponent