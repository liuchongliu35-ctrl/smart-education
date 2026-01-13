import style from './class.module.css'
import {Button, ConfigProvider, Pagination} from 'antd'
import {ArrowLeftOutlined,UploadOutlined} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import Example from '@/assets/png/13.png'
import homeworklistJSON from './homeworkList.json'
import { useEffect, useState } from 'react';



const ExamList = () =>{
          /* ———————通用——————— */
          const navigate = useNavigate()
          let {c_id} = useParams()
          
          /* ———————分行——————— */
          const list = homeworklistJSON
          const [currentPage, setCurrentPage] = useState(1);
          const pageSize = 6

        //    计算当前页数据
  const getCurrentPageData = () => {
    const startIndex = (currentPage - 1) * pageSize
    const endIndex = startIndex + pageSize
    return list.slice(startIndex, endIndex)
  }
    return(
        <ConfigProvider>
            
            <div style={{width:'100%',height:'100%',display:'flex'}}>
               <div style={{flex:1}}></div>
               <div style={{width:'80%',height:'100%'}}>
                <div style={{fontFamily:'siyuan',fontSize:16,paddingBlockStart:30,textAlign:'left',marginBottom:10}}><span style={{marginRight:10,color:'#0B2273',fontSize:20,cursor:'pointer'}} onClick={()=>navigate(`/class/${c_id}`)}><ArrowLeftOutlined /></span>考试</div>
                <div>
                {getCurrentPageData()?.map((item,index)=>(
                          <div className={style.listItem} key={index}>
                          {/* <div style={{width:100,height:80,border:'1px solid black'}}><img alt='#' src={Example} style={{width:60}} /></div> */}
                          <div style={{flex:1,height:80,padding:10,display:'flex',flexDirection:'column'}}>
                          <div style={{textAlign:'left',fontSize:21,fontWeight:'bold',lineHeight:1,fontFamily:'siyuan',height:'max-content'}}>{item.state === 1 ? <span className={style.ingTag}>进行中</span> : <span className={style.endTag}>已结束</span>}{item.h_name}（{item.score}分）</div>
                          <div style={{flex:1}}></div>
                          <div style={{lineHeight:1,display:'flex',color:'#757575',fontSize:12,textAlign:'left'}}>
                              <span >截止时间: {item.deadline}</span>
                              <span style={{flex:1}}></span>
                              <span style={{marginRight:20}}>已提交： 0</span>
                              <span >未提交： 56</span>
                              </div>
                          </div>
                          <div style={{flex:0.5,height:80,display:'flex',justifyContent:'center',alignItems:'center'}}>
                          <button  className={style.functionBtn}>查看作业</button>
                          <button  className={style.functionBtn}>作答情况</button>
                          </div>
      
                      </div>
                ))}
                </div>
                <Pagination
                    current={currentPage}
                    pageSize={pageSize}
                    total={list.length}
                    onChange={(page) => setCurrentPage(page)}
                    style={{ marginTop: 20 }}
                    align="center"
                />
               </div>
               <div style={{flex:1}}></div>

            </div>
        </ConfigProvider>
    )
}

export default ExamList