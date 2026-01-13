import { useNavigate } from 'react-router-dom'
import Icon, { DiffOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons';
import { Button, ConfigProvider, Pagination, Avatar, Row, Statistic, Col } from 'antd'
import { renderClassListAvatar } from '../../component/class/renderAvatar'
import style from './class.module.css'
import { AddIcon } from '../../assets/icons'
import { getClassList } from '@/apis/class'
import { useEffect, useState } from 'react'


const ClassEntry = () => {
  const uid = sessionStorage.getItem('uid')
  const navigate = useNavigate()
  const [classList, setClassList] = useState([])

  /* ——————数据请求———————— */
  const getList = async () => {
    let { data } = await getClassList(uid)
    setClassList(data.list)
    console.log(data)
  }

  useEffect(() => {
    getList()
  }, [])

  return (
    <ConfigProvider
      theme={{
        components: {
          Button: {

          }
        },
      }}
    >


      <div className={style.selectClassBox}>
        <div className={style.entryClassTitle}>选择班级</div>
        <div className={style.classBox}>
          <div style={{ minWidth: 600, display: 'flex', flexDirection: 'column' }} >
            {classList?.map((item, index) => (
              <div className={style.classCard} key={index}>
                <div className={style.cardLeft}>
                  <div style={{ lineHeight: 6 }}>
                    {renderClassListAvatar(item.cid)}
                  </div>
                  <div style={{ fontSize: 20, fontWeight: 500, letterSpacing: 1.5 }}>{item.cname}</div>
                  <div style={{ marginTop: 10 }}>
                    <Button className={style.glassEffect} onClick={() => navigate(`/class/${item.cid}`)}>查看班级</Button>
                  </div>
                </div>
                <div className={style.cardRight}>
                  <Row gutter={32} style={{ width: '100%' }}>
                    <Col span={8}>
                      <Statistic title="班级人数" value={item.person} prefix={<TeamOutlined />} />
                    </Col>
                    <Col span={8}>
                      <Statistic title="加课码" value={item.shortCode} />
                    </Col>
                    <Col span={8}>
                      <Statistic title="教学科目" value={item.csubject} className={style.subjectDes} />
                    </Col>
                  </Row>
                </div>
              </div>
            ))}
            <div className={style.addCard}>
              <div className={style.addCardglass}>
                <AddIcon style={{ fontSize: 30 }} />
              </div>
            </div>

          </div>
        </div>
      </div>



    </ConfigProvider>
  )
}
export default ClassEntry