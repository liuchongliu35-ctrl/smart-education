import { Avatar, Breadcrumb, Button, Col, ConfigProvider, Divider, Menu, QRCode, Row, Select } from "antd"
import { Outlet, useLocation, useNavigate, useParams } from "react-router-dom"
import style from './class.module.css'
import Icon, { TeamOutlined, LeftOutlined, ContainerOutlined, PlusCircleOutlined } from '@ant-design/icons';
import ClassRankLine from "../../component/charts/classRank";
import WrongPie from "../../component/charts/wrongPie";
import { FirstIcon, SecondIcon, ThirdIcon } from "@/assets/icons";
import Chahua from '../../assets/png/智能12.png'
import MemberIcon from '../../assets/svg/高效协同.svg'
import PreIcon from '../../assets/svg/文档储存.svg'
import HomeworkIcon from '../../assets/svg/办公文档.svg'
import ExamIcon from '../../assets/svg/考试.svg'
import { useEffect, useState } from "react"
import { getClassInfo } from "../../apis/class"
import ModalLoadingComponent from '@/component/Loading/modalLoading'


const workList = [
  { value: 'jack', label: 'Jack' },
  { value: 'lucy', label: 'Lucy' },
  { value: 'Yiminghe', label: 'yiminghe' },
  { value: 'disabled', label: 'Disabled' },
]

const ExcellentSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>excellent icon</title>
    <path d="M286.1 658.1s-25.7-99.2 58.2-77c0 0 148.8 80.6 335.4 0 83.9-22.3 58.2 77 58.2 77C690 805.3 512 793.3 512 793.3s-178 12-225.9-135.2z m54.2-270.7c0-29.7 24.1-53.8 53.8-53.8 29.7 0 53.8 24.1 53.8 53.8 0 29.7-24.1 53.8-53.8 53.8-29.8 0-53.8-24.1-53.8-53.8z m244 0c0-29.7 24.1-53.8 53.8-53.8 29.7 0 53.8 24.1 53.8 53.8 0 29.7-24.1 53.8-53.8 53.8-29.7 0-53.8-24.1-53.8-53.8zM803.8 962H220.2C133 962 62 891 62 803.8V220.2C62 133 133 62 220.2 62h583.5C891 62 962 133 962 220.2v583.5C962 891 891 962 803.8 962zM220.2 136.5c-46.2 0-83.8 37.5-83.8 83.7v583.6c0 46.2 37.6 83.8 83.8 83.8h583.5c46.2 0 83.8-37.6 83.8-83.8V220.2c0-46.2-37.6-83.8-83.8-83.8H220.2v0.1z m0 0" p-id="1025" fill="#ffffff"></path>
  </svg>
)
const WarnSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>excellent icon</title>
    <path d="M521.2 567c87.5 0 165.6 49.8 199 127 5.7 13.2-0.3 28.5-13.6 34.2-13.2 5.7-28.5-0.4-34.2-13.6-25.1-58.1-84.5-95.6-151.2-95.6-68.3 0-128 37.5-152.1 95.6-4.2 10-13.9 16-24 16.1-3.4 0-6.7-0.6-10-2-13.3-5.6-19.6-20.8-14.1-34.1C353.2 617.1 431.8 567 521.2 567zM312.5 342.3c-9.5-0.6-16.8-8.9-16.1-18.5 0.6-9.5 8.8-16.8 18.4-16.1 0.8 0 75.3 4.3 103.4-34.4 5.6-7.8 16.5-9.4 24.2-3.9 7.8 5.6 9.5 16.5 3.9 24.2-32.3 44.5-99 48.9-124 48.9-5.6 0-9.1-0.2-9.8-0.2z m135.4 62.4c0 11.4-3.6 22-9.7 30.7-9.8-13.4-25.6-22.2-43.5-22.2-18.3 0-34.5 9.2-44.2 23.1-6.5-8.9-10.3-19.8-10.3-31.6 0-29.7 24.1-53.8 53.8-53.8 29.8 0 53.9 24.1 53.9 53.8zM586 293.3c-5.6-7.8-3.9-18.6 3.9-24.2 7.8-5.6 18.6-3.9 24.2 3.9 28 38.7 102.6 34.4 103.4 34.3 9.7-0.6 17.8 6.7 18.4 16.2 0.6 9.5-6.6 17.8-16.2 18.5-0.7 0-4.2 0.2-9.7 0.2-25 0-91.7-4.4-124-48.9z m52.2 57.3c29.7 0 53.8 24.1 53.8 53.8 0 11.8-3.9 22.8-10.3 31.6-9.7-14-25.9-23.1-44.2-23.1-17.9 0-33.7 8.8-43.5 22.3-6-8.7-9.6-19.3-9.6-30.7 0-29.8 24.1-53.9 53.8-53.9zM339.9 570.7c-31.3 0-31.3-34.7-31.3-34.7 0-29.8 39.5-65.4 39.5-65.4 3.8 23.9 13.2 37.7 13.2 37.7 10.5 15.8 10.5 30.4 10.5 30.4 0.1 32.3-31.9 32-31.9 32zM803.8 962H220.2C133 962 62 891 62 803.8V220.2C62 133 133 62 220.2 62h583.5C891 62 962 133 962 220.2v583.5C962 891 891 962 803.8 962zM220.2 136.5c-46.2 0-83.8 37.5-83.8 83.7v583.6c0 46.2 37.6 83.8 83.8 83.8h583.5c46.2 0 83.8-37.6 83.8-83.8V220.2c0-46.2-37.6-83.8-83.8-83.8H220.2v0.1z m0 0" p-id="1428" fill="#ffffff"></path>
  </svg>
)

const ExcellentIcon = (props) => <Icon component={ExcellentSvg} {...props} />
const WarnIcon = (props) => <Icon component={WarnSvg} {...props} />

const LoadingComponent = () => {
  return (
    <div style={{ width: '100%', height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <ModalLoadingComponent />
    </div>
  )
}

const entryItemList = [
  { icon: <Avatar size={90} src={MemberIcon} />, title: '班级成员', text: 'Class Member' },
  { icon: <Avatar size={90} src={PreIcon} />, title: '预习任务', text: 'Preview Task' },
  { icon: <Avatar size={90} src={HomeworkIcon} />, title: '课后习题', text: 'Homework' },
  { icon: <Avatar size={90} src={ExamIcon} />, title: '考试', text: 'Exam' }
]


const ClassContent = () => {
  /* ———————通用——————— */
  const navigate = useNavigate()
  let { c_id } = useParams()
  const [isLoading, setIsLoading] = useState(false)
  const [classInfo, setClassInfo] = useState({
    cname: '暂无信息',
    shortCode: '暂无信息',
    person: '暂无信息',
  })
  /* ——————数据请求———————— */
  const getInfo = async () => {
    setIsLoading(true)
    let { data } = await getClassInfo(c_id)
    setClassInfo(data)
    setIsLoading(false)
  }

  useEffect(() => {
    getInfo()
  }, [])

  /* ———————分行——————— */
  function handleNavigate(e) {
    switch (e) {
      case 0:
        navigate(`/class/${c_id}/member`)
        break;
      case 1:
        navigate(`/class/${c_id}/preview`)
        break;
      case 2:
        navigate(`/class/${c_id}/exercise`)
        break;
      case 3:
        navigate(`/class/${c_id}/exam`)
        break;
      default:
        break;
    }
  }

  return (
    <ConfigProvider>
      {isLoading ? <LoadingComponent /> : <>
        <div style={{ width: '100%', height: 40, margin: 'auto', paddingTop: 20, display: 'flex', alignItems: 'center' }}>
          <span className={style.navigateIcon} onClick={() => navigate('/home/classentry')}><LeftOutlined /></span><span style={{ fontWeight: 600, fontSize: 16, color: '#494949' }}>{classInfo.cname}</span>
        </div>
        <div className={style.topArea}>
          <div className={style.entryCard}>
            <div style={{ width: '100%', textAlign: 'left', fontSize: 22, fontFamily: 'siyuan' }}>快速入口</div>
            <div className={style.entryList}>
              {entryItemList.map((item, index) => (
                <div className={style.entryItem} key={index} onClick={() => handleNavigate(index)}>
                  <div style={{ fontSize: 60, lineHeight: 1, marginRight: 5 }}>
                    {item.icon}
                  </div>
                  <div style={{ height: 'max-content', color: '#435fff' }}>
                    <div style={{ fontWeight: 600, fontSize: 16 }}>{item.title}</div>
                    <div style={{ fontSize: 10, marginTop: 10 }}>{item.text}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
          <div className={style.infoCard}>
            <div style={{ width: '40%', height: '80%', borderRight: '1px solid #eaeaea' }}>
              <div style={{ width: 'max-content', margin: 'auto' }}>
                <QRCode
                  size={100}
                  errorLevel="H"
                  value="https://ant.design/"
                />
              </div>
              <div style={{ fontSize: 12, fontFamily: 'siyuan', width: 'max-content', margin: 'auto', marginTop: 10 }}>加课码：<span>{classInfo.shortCode}</span></div>
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: 10 }}>
                <button className={style.inviteBtn}><PlusCircleOutlined style={{
                  marginRight: 5

                }} />邀请学生</button>
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '55%', margin: 'auto', marginBottom: 5 }}>
              <Avatar size={80} icon={<TeamOutlined />} style={{ background: '#f56a00' }} />
              <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                <div style={{ fontSize: 18, fontWeight: 600, textAlign: 'center' }}>{classInfo.cname}</div>
                <div style={{ color: '#636363', textAlign: 'center', fontFamily: 'SiYuan' }}>{classInfo.shortCode}</div>
                <div style={{ textAlign: 'center', fontWeight: 'bolder', marginTop: 10, fontSize: 14 }}>班级人数：<span style={{ fontSize: 20, fontFamily: 'youshe', color: '#651fff' }}>{classInfo.person}</span></div>
              </div>
            </div>
          </div>
        </div>
        <div className={style.topArea} style={{ height: 360 }}>
          <div style={{ flex: 0.96, height: '100%', borderRadius: 10, background: '#fff', padding: 20 }}>
            <ClassRankLine />
          </div>
          <div style={{ width: 300, borderRadius: 10 }} className={style.zhanshiCard}>
            <img src={Chahua} alt="#" style={{ width: 240 }} />
            <div className={style.zhanshiBtn}>
              <div style={{ fontFamily: 'youshe', color: '#fff', fontSize: 20 }}>智能分析报告</div>
              <div style={{ fontFamily: 'youshe', color: '#fff', fontSize: 14 }}>AI Report</div>
            </div>
          </div>
        </div>

        <div className={style.topArea} style={{ height: 360 }}>
          <div style={{ flex: 0.95, height: '100%', borderRadius: 10, background: '#fff', padding: 20, paddingTop: 30 }}>
            <div style={{ width: '100%', display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
              <div className={style.excllentStudent}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: 10 }}>
                  <Avatar size={36} icon={<ExcellentIcon />} style={{ background: '#4BDCB1' }} />
                  <span style={{ fontWeight: 600, fontSize: 20, marginLeft: 8 }}>上进者</span>
                </div>
                <div style={{ width: '90%', margin: 'auto' }}>
                  <div className={style.excllentStudentItem}>
                    <FirstIcon style={{ fontSize: 26 }} />
                    <span style={{ width: 60 }}>马灿</span>
                    <button className={style.checkBtn}>查看答题</button>
                  </div>
                  <div className={style.excllentStudentItem}>
                    <SecondIcon style={{ fontSize: 26 }} />
                    <span style={{ width: 60 }}>张明</span>
                    <button className={style.checkBtn}>查看答题</button>
                  </div>
                  <div className={style.excllentStudentItem}>
                    <ThirdIcon style={{ fontSize: 26 }} />
                    <span style={{ width: 60 }}>陈华</span>
                    <button className={style.checkBtn}>查看答题</button>
                  </div>
                </div>
              </div>

              <div style={{ width: '48%' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: 10 }}>
                  <Avatar size={36} icon={<WarnIcon />} style={{ background: '#df3131' }} />
                  <span style={{ fontWeight: 600, fontSize: 20, marginLeft: 8 }}>懒惰者</span>
                </div>
                <div style={{ width: '90%', margin: 'auto' }}>
                  <div className={style.excllentStudentItem}>
                    <span className={style.back}>18</span>
                    <span style={{ width: 60 }}>张婷</span>
                    <button className={style.checkBtn}>查看答题</button>
                  </div>
                  <div className={style.excllentStudentItem}>
                    <span className={style.back}>19</span>
                    <span style={{ width: 60 }}>刘洋</span>
                    <button className={style.checkBtn}>查看答题</button>
                  </div>
                  <div className={style.excllentStudentItem}>
                    <span className={style.back}>20</span>
                    <span style={{ width: 60 }}>赵阳</span>
                    <button className={style.checkBtn}>查看答题</button>
                  </div>
                </div>
              </div>
            </div>
            <div style={{ width: '80%', margin: 'auto', display: 'flex', justifyContent: 'center' }}><Button>查看更多</Button></div>
          </div>
          <div style={{ width: 500, borderRadius: 10, background: '#fff', position: 'relative', paddingTop: 10 }}>
            <Select
              prefix={<ContainerOutlined />}
              style={{ width: 140, position: 'absolute', right: 20, zIndex: 99 }}
              options={workList} />
            <div style={{ height: 3 }}>
              <div style={{ width: '100%', textAlign: 'left', fontSize: 20, fontWeight: 600, marginLeft: 20 }}>习题错误率</div>
            </div>
            <WrongPie />
          </div>
        </div>
      </>
      }
    </ConfigProvider>
  )
}

export default ClassContent