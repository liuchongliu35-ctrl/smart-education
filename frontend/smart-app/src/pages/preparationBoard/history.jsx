import { useNavigate } from 'react-router-dom'
import style from './preparationBoard.module.css'
import Icon, { RestOutlined, UploadOutlined } from '@ant-design/icons'
import { Button, ConfigProvider, Space, Table, Tag } from 'antd'
import designData from '@/assets/JSON/10条教学设计.json'
import { getAllDesignList } from '../../apis/preparation'
import { useEffect, useState } from 'react'


const HeartSvg = () => (
  <svg t="1740647456914" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5372" width="64" height="64"><path d="M548.256 175.808l-56.576 56.56 259.728 259.728L491.68 751.808l56.56 56.56 316.304-316.272-316.288-316.288z m-304.016 0l-56.56 56.56 259.728 259.728L187.68 751.808l56.56 56.56 316.288-316.272-316.288-316.288z" fill="#565D64" p-id="5373"></path></svg>
)

const QuitIcon = (props) => <Icon component={HeartSvg} {...props} />

const columns = [
  {
    title: '教案名称',
    dataIndex: 'designName',
    key: 'designName',
    render: (text) => <a>{text}</a>,
    align: 'center'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    align: 'center'

  },
  {
    title: '最近修改',
    dataIndex: 'classTime',
    key: 'classTime',
    align: 'center'

  },
  {
    title: 'Action',
    key: 'action',
    align: 'center',
    render: (_, record) => (
      <Space size="middle">
        <button className={style.functionBtn}><UploadOutlined style={{ marginRight: 2, fontSize: 16 }} />生成PPT</button>
        <button className={style.deleteBtn}><RestOutlined style={{ marginRight: 2, fontSize: 16 }} />删除</button>
      </Space>
    )
  }

];
const boradList = designData




const PreparationBoardHistory = () => {
  const navigate = useNavigate()
  const [historyList, setHistoryList] = useState([])
  const uid = sessionStorage.getItem('uid')


  /* ——————数据请求———————— */
  const getDesignList = async () => {
    let { data } = await getAllDesignList(uid)
    setHistoryList(data.slice(0, 7))
    console.log(data)
  }

  useEffect(() => {
    getDesignList()
  }, [])

  return (
    <ConfigProvider>
      <QuitIcon className={style.quitIcon} onClick={() => navigate(-1)} style={{ color: 'hotpink' }} />

      <div style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
        <div className={style.box} style={{ height: 'max-content', paddingBottom: 30 }}>
          <div style={{ width: '100%', textAlign: 'left', fontSize: 22, fontFamily: 'siyuan', marginBottom: 20 }}>教学设计历史记录</div>

          <Table
            style={{ width: 1100, margin: 'auto', boxShadow: '0px 0px 10px #666666' }}
            columns={columns}
            dataSource={historyList}

          />
        </div>
      </div>
    </ConfigProvider>
  )
}

export default PreparationBoardHistory