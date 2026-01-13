import style from './class.module.css'
import {Avatar, ConfigProvider, Pagination} from 'antd'
import {ArrowLeftOutlined} from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import previewListJSON from './PreviewList.json'
import { useEffect, useState } from 'react'
import NoneDataIcon from '@/assets/svg/暂无内容.svg'
import { getClassHomeworkList } from '@/apis/class'
import ModalLoadingComponent from '@/component/Loading/modalLoading'


const LoadingComponent = () =>{
  return(
      <div style={{width:'100%',height:'100vh',display:'flex',justifyContent:'center',alignItems:'center'}}>
          <ModalLoadingComponent />
      </div>
  )
}

const HomeworkList = () =>{
    const [classPreList,setClassPreList] = useState([])
    const [isLoading,setIsLoading] = useState(false)

     /* ———————通用——————— */
     const navigate = useNavigate()
     let {c_id} = useParams()
     /* ——————数据请求———————— */
     const getPreList = async() =>{
       setIsLoading(true)
       let {data} = await getClassHomeworkList(c_id)
       setClassPreList(data)
       setIsLoading(false)
       console.log(data)
     }
    
      useEffect(() => {
       getPreList()
       }, [])

     
     const [currentPage, setCurrentPage] = useState(1);
     const pageSize = 6

        //    计算当前页数据
  const getCurrentPageData = () => {
    if(classPreList){
    
    const startIndex = (currentPage - 1) * pageSize
    const endIndex = startIndex + pageSize
    return classPreList.slice(startIndex, endIndex) }
  }
    return(
        <ConfigProvider>
       {isLoading ? <LoadingComponent /> : <>
            <div style={{width:'100%',height:'100%',display:'flex'}}>
               <div style={{flex:1}}></div>
               <div style={{width:'80%',height:'100%'}}>
                <div style={{fontFamily:'siyuan',fontSize:16,paddingBlockStart:30,textAlign:'left',marginBottom:10}}><span style={{marginRight:10,color:'#0B2273',fontSize:20,cursor:'pointer'}} onClick={()=>navigate(`/class/${c_id}`)}><ArrowLeftOutlined /></span>课后习题</div>
                {getCurrentPageData() && getCurrentPageData().length > 0 ? (<>
                <div>
                {getCurrentPageData()?.map((item,index)=>(
                          <div className={style.listItem} key={index}>
                          {/* <div style={{width:100,height:80,border:'1px solid black'}}><img alt='#' src={Example} style={{width:60}} /></div> */}
                          <div style={{flex:1,height:80,padding:10,display:'flex',flexDirection:'column'}}>
                          <div style={{textAlign:'left',fontSize:21,fontWeight:'bold',lineHeight:1,fontFamily:'siyuan',height:'max-content'}}>{item.active === 1 ? <span className={style.ingTag}>进行中</span> : <span className={style.endTag}>已结束</span>}{item.hname}<span style={{fontSize:14}}>（ {item.htitle}）</span></div>
                          <div style={{flex:1}}></div>
                          <div style={{lineHeight:1,display:'flex',color:'#757575',fontSize:12,textAlign:'left'}}>
                              <span >截止时间: {item.deadline}</span>
                              <span style={{flex:1}}></span>
                              <span style={{marginRight:20}}>已提交： {item.complate}</span>
                              <span >未提交： {item.uncomplate}</span>
                              </div>
                          </div>
                          <div style={{flex:0.5,height:80,display:'flex',justifyContent:'center',alignItems:'center'}}>
                          <button  className={style.functionBtn} onClick={()=>navigate(`/homework/${item.hid}`)}>查看作业</button>
                          <button  className={style.functionBtn} onClick={()=>navigate(`/class/${c_id}/homeworkdetail/${item.hid}`)}>答题详情</button>
                          </div>
                   
                      </div>
                ))}
                </div>
                <Pagination
                    current={currentPage}
                    pageSize={pageSize}
                    total={classPreList.length}
                    onChange={(page) => setCurrentPage(page)}
                    style={{ marginTop: 20 }}
                    align="center"
                />
 </>)
 : (
  <div style={{width:'100%',display:'flex',justifyContent:'center'}}>
    <Avatar src={NoneDataIcon} size={128} />
    <div style={{fontFamily:'youshe',color:'#000',fontSize:22}}>暂无数据</div>
  </div>
)} 
               </div>
               <div style={{flex:1}}></div>
            </div>
            </>
        }
        </ConfigProvider>
    )
}

export default HomeworkList