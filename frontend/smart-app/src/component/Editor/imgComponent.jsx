import { ConfigProvider, Input,Button, Empty, Typography, Avatar, Image, message, Popconfirm, Popover } from "antd"
import React, { useEffect, useState } from 'react';
import Icon, {SearchOutlined} from '@ant-design/icons'
import style from '@/pages/TextEditor/textEditor.module.css'
import ModalLoadingComponent from "../Loading/modalLoading"
import NoneDataIcon from '@/assets/svg/暂无数据.svg'
import { postGeneratePhoto } from "../../apis/preparation";
import 'ldrs/ring'
import { spiral } from 'ldrs'
spiral.register()



const ImgComponent = ({modifyContent,tdId}) =>{
  
  useEffect(() => {
    setText(modifyContent)
      
    }, [modifyContent])

    const [isGenerate,setIsGenerate] = useState('0')
    const [text,setText] = useState('')
    const [imgList,setImgList] = useState([])
    const [messageApi, contextHolder] = message.useMessage()
    

    
    
    const generatePhoto = async() =>{
            setIsGenerate('1')
            const prompt = {
                text:text
            }
            let {data} = await postGeneratePhoto(prompt)
            setImgList(data)
            setTimeout(() => {
                setIsGenerate('2')
                setText('')
            }, 50);
        }
        const handleNativeCopy = async () => {
          messageApi.open({
            type: 'success',
            content: '复制成功',
          })
         
        }
  
    

    return(
        <ConfigProvider>
            {contextHolder}
            <div style={{width:'90%',margin:'auto',height:'max-content'}}>
            <div style={{width:'100%',height:40,fontFamily:'siyuan',lineHeight:'40px',paddingBottom:10,borderBottom:'1px solid #eaeaea',fontSize:13.5}}>
              图片生成</div>
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
          {/* <div style={{width:'100%',display:'flex',justifyContent:'center',marginTop:30}}>
          <button  className={style.functionBtn} style={{fontSize:16}} onClick={()=>generateBtn()}>点击生成</button>
          </div> */}
           
            <div style={{flex:1,display:'flex',justifyContent:'center',alignItems:'center'}}>
            <div style={{width:'100%',flex:1,display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center'}}>         
             <Avatar src={NoneDataIcon} size={128} />
            <div style={{fontFamily:'youshe',color:'#000',fontSize:22}}>暂无内容</div>
            </div>
            
         </div> 
         </>}
            {isGenerate === '2' && <div style={{width:'100%',paddingInline:20,flex:1,display:'flex',flexDirection:'column',alignItems:'center',minHeight:0,overflowY:'auto'}}>
                {imgList?.map((item,index)=>(
                  <div key={index}>
                   <div style={{marginTop:20,marginBottom:10,fontSize:14,fontWeight:600,textAlign:'left',width:'100%'}}>{item.key}</div>
                  <div style={{
                    display:'grid',
                    width:'100%',
                    gridTemplateColumns:'repeat(auto-fill, minmax(120px,1fr))',
                    gap:'10px', //设置图片之间的间距
                    alignItems:'start', //对齐方式为顶部对齐
                  }}>
                    {item.photoUrl.slice(1, 7).map((url, imgIndex) => (  
                                    <div key={imgIndex} style={{ position: 'relative' }}>  
                                       <Popover
                content={<div style={{width:'max-content',textAlign:'center',fontSize:12}}><button className={style.functionBtn} onClick={()=>handleNativeCopy()}>复制</button></div>}> 
                <Image  style={{ width: '100%', height: 'auto', objectFit: 'cover', borderRadius: '8px' }}  alt="出错了"  src={url}  /> 
                                      </Popover>
                               
                                       
                                    </div>  
                                ))}  
                </div>
                  </div>
                ))}
                
               
       
              </div>}
            
 
        </ConfigProvider>
    )
}

export default ImgComponent