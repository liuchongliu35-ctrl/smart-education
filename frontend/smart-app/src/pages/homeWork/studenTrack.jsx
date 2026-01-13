import { Button, Collapse, ConfigProvider } from "antd"
import style from './homeWork.module.css'
import workExampleJSON from '../../assets/JSON/第一次作业.json'
import ModalLoadingComponent from '@/component/Loading/modalLoading'
import { Children, useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { getStuPreviewDetail } from "../../apis/class"
import { MarkdownToHtml } from "../../component/Editor/changeMarkdown"
import InfoIcon from '../../assets/svg/搜索.svg'
import { ArrowRightOutlined,CaretRightOutlined } from '@ant-design/icons'


const LoadingComponent = () =>{
    return(
        <div style={{width:'100%',height:'100vh',display:'flex',justifyContent:'center',alignItems:'center'}}>
            <ModalLoadingComponent />
        </div>
    )
}

const StudentTrackPage = () =>{
    let {u_id,w_id} = useParams()
    const navigate = useNavigate()
    const list = workExampleJSON
    const [isLoading,setIsLoading] = useState(false)
    const [questionList,setQuestionList] = useState([])
    const [trackContent,setTrackContent] = useState({})
    const [stuName,setStuName] = useState('')
    const [completeTime,setCompleteTime] = useState('')
    const [trueNum,setTrueNum] = useState('')
    const [previewName,setPreviewName] = useState('')
    const [mistakePoint,setMistakepoint] = useState('')
    const [info,setInfo] = useState('')

    //数据请求
    const getDetail = async () =>{
      let {data} = await getStuPreviewDetail(u_id,w_id)
      console.log(data)
      setQuestionList(data.questionTrackList)
      setTrackContent(data.track)
      setStuName(data.stuName)
      setCompleteTime(data.completeTime)
      setTrueNum(data.trueNum)
      setPreviewName(data.previewName)
      setMistakepoint(data.mistakePoint)
      setInfo(data)
    }

    

    useEffect(() => {
        setIsLoading(true)
        getDetail()
        setTimeout(() => {
        setIsLoading(false)
        }, 1000);
      }, [])

      function renderMistake(content) {  
        if (content) {  
          const mistakePoints = content;  
      
          return (  
            <div>  
              {mistakePoints.map((item, index) => (  
                // 遍历mistakePoints，并解析键和值  
                Object.keys(item).map((key) => {  
                  const value = item[key];  
                  return (  
                    <div key={index}>  
                      <div style={{marginLeft:10}}><span style={{fontFamily:'youshe'}}>{value}、</span>{key}</div>  
                    </div>  
                  );  
                })  
              ))}  
            </div>  
          );  
        }  
      } 

    const items = [
        {
            key:'1',
            label:<div className={style.reportTag}>考察内容</div>,
            children:<div>
            <div style={{fontSize:15,fontFamily:'siyuan',color:'#000'}}>{info.ptitle}</div>
            <div style={{fontSize:14,fontFamily:'siyuan',marginTop:10}}>{info.secondaryTitle}</div>
            </div>
        },{
            key:'2',
            label:<div className={style.reportTag}>薄弱点</div>,
            children: <div style={{fontSize:14}}>   
            {renderMistake(mistakePoint)}
            </div>
        },{
            key:'3',
            label:<div className={style.reportTag}>分析报告</div>,
            children:<div style={{fontSize:14}}>
            <MarkdownToHtml content={trackContent.aiAnalysis} />
            </div>
        },{
            key:'4',
            label:<div className={style.reportTag}>AI补充资料</div>,
            children:<div style={{fontSize:14}}>
            <MarkdownToHtml content={trackContent.supplement} />
            </div>
        }
    ]

    

    return(
        <ConfigProvider>
            {isLoading ? <LoadingComponent /> :
            <div style={{background:'#F7F8FC',display:'flex',position:'relative'}}>
            <div style={{width:'34%',height:'100vh',position:'fixed',left:'1%',background:'#fff',boxShadow:'0px 0px 4px #0000003b',minHeight:0,overflowY:'auto',padding:20}}>
            <div className={style.info}>
            <div style={{flex:1,height:'max-content',textAlign:'left'}}>

                <div>姓名:{stuName} </div>
                <div>作答时间：<span>{completeTime}</span></div>
                <div>得分：<span>{trackContent.questionScore}/{info.score}</span></div>
                <div>答对数量：<span>{trueNum}</span></div>
                </div>
                 <div style={{width:100}}>
                                <img src={InfoIcon} alt='#' style={{width:86}}/>
                  </div>
              
            </div>
            <Collapse 
            style={{marginTop:30}}
            ghost 
            items={items} 
            expandIcon={({ isActive }) => <CaretRightOutlined style={{color:''}} rotate={isActive ? 90 : 0} />}
            />
    
            </div>
            <div style={{position:'absolute',right:30,top:30,fontSize:18,fontWeight:600}}>
                {previewName}
                <button className={style.defBtn} onClick={()=>navigate(-1)} style={{width:40,height:40,fontSize:18}} ><ArrowRightOutlined /></button>
            </div>
            <div style={{width:'60%',marginLeft:'38%',marginTop:80,padding:60,background:"#fff",boxShadow:'0px 0px 4px #0000003b',display:'flex',flexDirection:'column'}}>
                {questionList?.map((item,index)=>(
                       <div className={item.isCorrect === 1 ? style.exerciseItemBox : style.exerciseWrongItemBox} style={{position:'relative'}}>
                       <div className={item.isCorrect === 1 ? style.exerciseItemLogo : style.exerciseWrongItemLogo}><span style={{marginRight:16}}> </span><span>{index+1}.{item.homeworkDetails.qtype}</span><span style={{fontSize:14,fontWeight:500}}>（{item.homeworkDetails.defaultScore}分）</span></div>
                       <div style={{fontFamily:'siyuan',lineHeight:1.5,marginBottom:15}}>{item.homeworkDetails.qcontent}</div>
                       <div style={{fontFamily:'siyuan',lineHeight:1.5,marginBottom:15}}>{item.homeworkDetails.selections}</div>
                       <div> 
                       {item.isCorrect === 0 && <div style={{width:'100%',height:'max-content',fontSize:15}}>
                            <div style={{padding:10,fontWeight:600,color:'#fd5252'}}>学生答案：{item.answer}</div>
                           <div style={{padding:10,color:'#585858'}}>正确答案：{item.homeworkDetails.correctAnswer}</div>
                           <div style={{width:'100%',height:'max-content',fontSize:15}}>
                           <div style={{padding:10}}>解析：{item.homeworkDetails.answerAnalysis}</div>
                       </div>
                           <div style={{padding:10,fontSize:14}}><span style={{fontSize:14,fontWeight:'bolder'}}>错误分析</span><MarkdownToHtml content={item.mistakeCase} /></div>
                       </div>}
                       {item.isCorrect === 1 && <div style={{width:'100%',height:'max-content',fontSize:15}}>
                            <div style={{padding:10,fontWeight:550,color:'#435fff'}}>学生答案：{item.answer}</div>
                           <div style={{padding:10,color:'#585858',fontSize:14}}>正确答案：{item.homeworkDetails.correctAnswer}</div>
                           <div style={{width:'100%',height:'max-content',fontSize:15}}>
                           <div style={{padding:10}}>解析：{item.homeworkDetails.answerAnalysis}</div>
                       </div>
                       </div>}
                       </div>
                       <div style={{position:'absolute',top:10,right:20,fontSize:13}}>{item.isCorrect === 0 ? 
                        <span style={{color:'#fd5252'}}>错误</span> : 
                        <span style={{color:'#435fff'}}>正确</span>
                        }</div>
                   </div>
                ))}
             
            </div>
            </div>
            }
            
              
        </ConfigProvider>
    )
}

export default StudentTrackPage