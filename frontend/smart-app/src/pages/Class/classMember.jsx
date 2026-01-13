import style from './class.module.css'
import {Button, ConfigProvider, Space, Table, Tag} from 'antd'
import {ArrowLeftOutlined} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getClassMemberList } from '../../apis/class';
import { useEffect, useState } from 'react';

const columns = [
    
    {
      title: '姓名',
      dataIndex: 'stuName',
      key: 'stuName',
      render: (text) => <a>{text}</a>,
      align:'center'
    },
    {
      title: '学号',
      dataIndex: 'stuNum',
      key: 'stuNum',
      align:'center'
    },
    {
      title: '排行',
      dataIndex: 'ranking',
      key: 'ranking',
      align:'center',
      sorter: (a, b) => a.ranking - b.ranking,
      defaultSortOrder: 'ascend', // 可选默认排序
    },
    {
      title: '邮箱',
      key: 'email',
      dataIndex: 'email',
      align:'center',
      render: () => <span>--</span>,
    },{
      title: '当前状态',
      dataIndex: 'evaluation',
      key: 'evaluation',
      align:'center'
    }
    
  ]

const boradList = [
    {studentName:'陆子轩',studentCode:'230001',gender:'男',email:'--'},
    {studentName:'欧阳雪',studentCode:'230002',gender:'女',email:'--'},
    {studentName:'陈浩然',studentCode:'230003',gender:'男',email:'--'},
    {studentName:'张晓丽',studentCode:'230004',gender:'女',email:'--'},
    {studentName:'司马青',studentCode:'230005',gender:'男',email:'--'},
    ]



const ClassMemberPage = () =>{
          const [memberList,setMemberList] = useState('')
          /* ———————通用——————— */
          const navigate = useNavigate()
          let {c_id} = useParams()
          console.log(c_id)
          /* ——————数据请求———————— */
            const getMemberList = async() =>{
              let {data} = await getClassMemberList(c_id)
              setMemberList(data)
              console.log(data)
            }
          
             useEffect(() => {
              getMemberList()
              }, [])
          
    return(
        <ConfigProvider>
            
            <div style={{width:'100%',height:'100%',display:'flex'}}>
               <div style={{flex:1}}></div>
               <div style={{width:'80%',height:'100%'}}>
                <div style={{fontFamily:'siyuan',fontSize:18,paddingBlockStart:30}}><span style={{marginRight:10,color:'#0B2273',fontSize:18,cursor:'pointer'}} onClick={()=>navigate(`/class/${c_id}`)}><ArrowLeftOutlined /></span>班级成员</div>
                  <Table
                        size={'middle'}
                        style={{marginTop:20}}
                        columns={columns}
                        dataSource={memberList}
                      />
               </div>
               <div style={{flex:1}}></div>

            </div>
        </ConfigProvider>
    )
}

export default ClassMemberPage