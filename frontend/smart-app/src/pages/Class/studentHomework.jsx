import style from './class.module.css'
import {Button, ConfigProvider, Pagination, Table} from 'antd'
import {ArrowLeftOutlined,UploadOutlined} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import detailList from './virtualJSON/previewDetail.json'
import { getHomeworkDetailTable,getHomeworkDetailContent } from '../../apis/class';
import PassPie from '../../component/charts/passPie';
import PageLoadingComponent from '../../component/Loading/pageLoadingComponent';

const boradList = detailList


const StudentHomeworkDetail = () =>{
     let {h_id,c_id} = useParams()

    const columns = [
    
        {
          title: '姓名',
          dataIndex: 'name',
          key: 'name',
          align:'center'
        },
        {
          title: '学号',
          dataIndex: 'stuCode',
          key: 'stuCode',
          align:'center'
      
        },
        {
          title: '得分',
          dataIndex: 'score',
          key: 'score',
          align:'center',
          sorter: (a, b) => b.score - a.score,
          defaultSortOrder: 'ascend',
          render: (text) => <span style={{color:'#435fff',fontFamily:'siyuan'}}>{text?text:'--'}</span>
        },
        {
          title: '作答时间',
          key: 'completeTime',
          dataIndex: 'completeTime',
          align:'center'
        },
        {
          title: '操作',
          key: 'function',
          dataIndex: 'function',
          align:'center',
          render:(text, record)=><div style={{height:30}}>
            {record.score &&
            <Button  style={{fontSize:12,lineHeight:1,border:'1px solid #435fff',color:'#435fff'}} onClick={()=>navigate(`/workdetail/${record.uid}/${record.hid}`)}>查看详情</Button>
          }
            </div>
        }
        
      ]
    
    const navigate = useNavigate()
    const [detailTable,setDetailTable] = useState([])
    const [detailContent,setDetailContent] = useState([])
    const [isLoading,setIsLoading] = useState(false)
    const [passRate,setPassRate] = useState([])
    //数据请求
    const getTable = async () =>{
      let {data} = await getHomeworkDetailTable(c_id,h_id)
      setDetailTable(data)
      console.log(data)
    }
    const getContent = async () =>{
      setIsLoading(true)
      let {data} = await getHomeworkDetailContent(c_id,h_id)
      setDetailContent(data)
      setPassRate([data.goodRate,data.passingRate,data.unPassingRate])
      setIsLoading(pre => !pre)

    }
  
    useEffect(() => {
        getTable()
        getContent()
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
                    <div style={{ marginBottom:10, fontSize: 18,fontWeight:'bolder' }}>
                      {key} 
                      <span style={{ fontFamily: 'youshe',marginLeft:10,fontSize:`${30 - index*2}px`,color:'#435fff' }}>{value}</span></div>  
                    </div>
                  );  
                })  
              ))}  
            </div>  
          );  
        }  
      } 


    return(
        <ConfigProvider>
    {isLoading ? <PageLoadingComponent /> : <>
          
            <div style={{width:'100%',height:'100%',display:'flex'}}>
               <div style={{flex:1}}></div>
               <div style={{width:'80%',height:'100%'}}>
                <div style={{fontFamily:'siyuan',fontSize:16,paddingBlockStart:30,textAlign:'left',marginBottom:10}}><span style={{marginRight:10,color:'#0B2273',fontSize:20,cursor:'pointer'}} onClick={()=>navigate(-1)}><ArrowLeftOutlined /></span>{detailContent.hname}</div>
                <div className={style.dataBox}>
                  <div>
                  <div style={{width:400,height:250}}>
                  <PassPie passRate={passRate}/>
                  </div>
                  <div style={{textAlign:'center',fontWeight:600,fontSize:16}}>作业及格情况</div>
                  </div>
                  <div>
                    <div style={{textAlign:'left',marginBottom:'1em'}}>
                      <div style={{fontSize:16,fontFamily:'siyuan'}}>作业完成人数</div>
                      <div style={{color:'#303030'}}>{detailContent.complete}</div>
                    </div>
                    <div style={{textAlign:'left',marginBottom:'1em'}}>
                      <div style={{fontSize:16,fontFamily:'siyuan'}}>平均得分</div>
                      <div style={{color:'#303030'}}>{detailContent.avgScore}</div>
                    </div>
                    <div style={{textAlign:'left',marginBottom:'1em'}}>
                      <div style={{fontSize:16,fontFamily:'siyuan'}}>优秀作业个数</div>
                      <div style={{color:'#303030'}}>{detailContent.good}</div>
                    </div>
                    <div style={{textAlign:'left'}}>
                      <div style={{fontSize:16,fontFamily:'siyuan'}}>中等作业个数</div>
                      <div style={{color:'#303030'}}>{detailContent.middle}</div>
                    </div>
                  </div>
                  <div style={{flex:1}}></div>
             
                  <div style={{marginRight:'14%',height:'80%'}}>
                    <div className={style.reportTag}>高频易错点</div>
                      <div>
                        {renderMistake(detailContent.mistakePoint)}
                      </div>
                  </div>
                </div>
                <div>
                <Table  
                        style={{boxShadow:'0px 0px 6px #00000026'}}
                        size={'middle'}
                        columns={columns}
                        dataSource={detailTable}
                      />
                </div>
              
               </div>
               <div style={{flex:1}}></div>

            </div>
            </>}   
        </ConfigProvider>
    )
}

export default StudentHomeworkDetail