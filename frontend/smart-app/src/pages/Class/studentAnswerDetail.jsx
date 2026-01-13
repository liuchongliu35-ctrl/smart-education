import style from './class.module.css'
import {Button, ConfigProvider, Pagination, Table} from 'antd'
import {ArrowLeftOutlined,UploadOutlined} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import detailList from './virtualJSON/previewDetail.json'
import { getPreviewDetailTable } from '../../apis/class';


const boradList = detailList


const StudentAnswerDetail = () =>{
     let {p_id,c_id} = useParams()

    const columns = [
    
        {
          title: '姓名',
          dataIndex: 'stuName',
          key: 'stuName',
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
          dataIndex: 'textScore',
          key: 'textScore',
          align:'center',
          sorter: (a, b) => b.textScore - a.textScore,
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
            {record.textScore &&
            <Button  style={{fontSize:12,lineHeight:1,border:'1px solid #435fff',color:'#435fff'}} onClick={()=>navigate(`/previewdetail/${record.sid}/${p_id}`)}>查看详情</Button>
          }
            </div>
        }
        
      ]
    
    const navigate = useNavigate()
    const [detailTable,setDetailTable] = useState([])
    //数据请求
    const getTable = async () =>{
      let {data} = await getPreviewDetailTable(p_id,c_id)
      setDetailTable(data)
    }
  
    useEffect(() => {
        getTable()
      }, [])

    return(
        <ConfigProvider>
            <div style={{width:'100%',height:'100%',display:'flex'}}>
               <div style={{flex:1}}></div>
               <div style={{width:'80%',height:'100%'}}>
                <div style={{fontFamily:'siyuan',fontSize:16,paddingBlockStart:30,textAlign:'left',marginBottom:10}}><span style={{marginRight:10,color:'#0B2273',fontSize:20,cursor:'pointer'}} onClick={()=>navigate(-1)}><ArrowLeftOutlined /></span>答题详情</div>
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
        </ConfigProvider>
    )
}

export default StudentAnswerDetail