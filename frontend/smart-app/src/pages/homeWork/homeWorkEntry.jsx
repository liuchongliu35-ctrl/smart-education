import { useNavigate, useParams } from 'react-router-dom'
import style from './homeWork.module.css'
import Icon, { RocketFilled, PlusCircleFilled, SearchOutlined } from '@ant-design/icons';
import { ConfigProvider, Modal, Segmented, Divider, Input, Avatar, message, Button } from 'antd'
import { useEffect, useState } from 'react'
import WorkSelectForm from '@/component/homeWork/workSelect'
import PreviewSelectForm from '@/component/homeWork/previewSelect'
import PreHistory from '../../component/homeWork/preHistory'
import HomeWorkHistory from '../../component/homeWork/homeworkHistory'
import ExamHistory from '../../component/homeWork/examHistory'
import PreIcon from '@/assets/svg/文档储存.svg'
import HomeworkIcon from '@/assets/svg/办公文档.svg'
import ExamIcon from '@/assets/svg/考试.svg'
import { getSubjectList, postCreatePreview, postCreateHomework, putPublishPre, putPublishWork } from '@/apis/homeworkAPI'
import { getClassList } from '@/apis/class'
import ModalLoadingComponent from '@/component/Loading/modalLoading'
import PublishForm from '@/component/homeWork/publishWork'
import PublishHomework from '../../component/homeWork/publishHomework'

const segmentItem = [
  {
    label: <div style={{ height: 50, fontWeight: 'bold', paddingBlock: 5, fontSize: 14, textWrap: 'wrap', lineHeight: 1.5 }}>
      预习任务
    </div>,
    value: '预习任务'
  },
  {
    label: <div style={{ height: 50, fontWeight: 'bold', paddingBlock: 5, fontSize: 14, textWrap: 'wrap', lineHeight: 1.5 }}>
      课后习题
    </div>,
    value: '课后习题'
  },
  {
    label: <div style={{ height: 50, fontWeight: 'bold', paddingBlock: 5, fontSize: 14, textWrap: 'wrap', lineHeight: 1, display: 'flex', alignItems: 'center' }}>
      考试
    </div>,
    value: '考试'
  },
]

const workItem = [
  { icon: <Avatar size={100} src={PreIcon} />, title: '预习任务', text: 'Preview Task' },
  { icon: <Avatar size={100} src={HomeworkIcon} />, title: '课后习题', text: 'Homework' },
  { icon: <Avatar size={100} src={ExamIcon} />, title: '考试', text: 'Exam' }
]




const HomeworkEntry = () => {
  let { tsId, schoolId } = useParams()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [sendOpen, setSendOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [reload, setReload] = useState(false)
  const [isPubLoading, setPubIsLoading] = useState(false)
  const [confirmLoading, setConfirmLoading] = useState(false)
  const [isShowWork, setIsShowWrok] = useState(false)
  const [modalTitle, setModalTitle] = useState('预习任务')
  const [listType, setListType] = useState(sessionStorage.getItem('currentList'))
  const [knowledgeData, setKnowledgeData] = useState([])
  const [classList, setClassList] = useState([])
  const [hid, setHid] = useState(null)
  const [messageApi, contextHolder] = message.useMessage()
  const [currentValue, setCurrentValue] = useState(sessionStorage.getItem('currentList'))
  const uid = sessionStorage.getItem('uid')
  const [wherePublish, setWherePublish] = useState(0)

  const handleChangeType = (value) => {
    setListType(value)
    sessionStorage.setItem('currentList', value)
  }

  useEffect(() => {
    setCurrentValue(sessionStorage.getItem('currentList'))
  }, [listType])


  /* ——————章节知识点数据请求———————— */
  const getSubjectItem = async () => {
    let { data } = await getSubjectList(tsId, schoolId)
    setKnowledgeData(data)
  }

  const getClass = async (uid) => {
    let { data } = await getClassList(uid)
    setClassList(data.list)
  }
  useEffect(() => {
    getSubjectItem()
    getClass(uid)
  }, [])

  //分辨习题考试函数
  function judgeType(e) {
    switch (e) {
      case 0:
        setModalTitle('预习任务')
        break;
      case 1:
        setModalTitle('课后习题')
        break;
      case 2:
        setModalTitle('考试')
        break;
      default:
        break;
    }
  }




  /* 弹窗控制区域 */
  const showModal = (e) => {
    if (e === 0) {
      setIsShowWrok(false)
    } else { setIsShowWrok(true) }

    judgeType(e)
    setTimeout(() => {
      setOpen(true)
    }, 50);
  }

  const handleOk = () => {
    setConfirmLoading(true)
    setTimeout(() => {
      setOpen(false)
      setConfirmLoading(false);
    }, 2000)
  }
  const handleCancel = () => {
    setOpen(false)
  }

  /* 发布预习任务习题弹窗,wherePublish = 0 */
  const showPublishModal = (e) => {
    setWherePublish(0)
    setHid(e)
    setTimeout(() => {
      setSendOpen(true)
    }, 50)
  }
  /* 发布课后习题弹窗,wherePublish = 1 */
  const showPublishHomework = (e) => {
    setWherePublish(1)
    setHid(e)
    setTimeout(() => {
      setSendOpen(true)
    }, 50)
  }

  const handlePublishCancel = () => {
    setSendOpen(false)
  }
  /* ——————分行———————— */

  const createPreview = async (prop, uid) => {
    setIsLoading(true)
    let { data } = await postCreatePreview(prop, uid)
    setTimeout(() => {
      navigate('/preview/0', {
        state: {
          content: data,
        }
      })
      setIsLoading(false)
      setOpen(false)
    }, 1000)
  }

  const createHomework = async (prop, uid) => {
    setIsLoading(true)
    let { data } = await postCreateHomework(prop, uid)
    console.log(data)
    setTimeout(() => {
      navigate('/homework/0', {
        state: {
          content: data,
        }
      })
      setIsLoading(false)
      setOpen(false)
    }, 1000)
  }

  //发布预习任务事件
  const publishPre = async (prop) => {
    setPubIsLoading(true)
    await putPublishPre(prop)
    setPubIsLoading(false)
    setSendOpen(false)
    setReload(pre => !pre)
    messageApi.open({
      type: 'success',
      content: '发布成功',
    })
  }

  //发布课后习题事件
  const publishWork = async (prop) => {
    setPubIsLoading(true)
    await putPublishWork(prop)
    setPubIsLoading(false)
    setSendOpen(false)
    setReload(pre => !pre)
    messageApi.open({
      type: 'success',
      content: '发布成功',
    })
  }


  return (
    <ConfigProvider
      theme={{
        components: {
          Segmented: {
            itemSelectedBg: '#6581ED',
            itemSelectedColor: '#fff',
            trackBg: "#fff"
          },
          Input: {
            paddingInline: 10
          }
        },
      }}
    >
      {contextHolder}

      <div style={{ width: '80%', margin: 'auto', color: '#000', marginTop: 30, textAlign: 'left', fontSize: 22, fontFamily: 'siyuan', lineHeight: 1 }}>
        选择习题类型</div>

      <div className={style.btnBox}>
        {workItem.map((item, index) => (
          <div className={style.entryItem} key={index} onClick={() => showModal(index)}>
            <div style={{ fontSize: 60, lineHeight: 1, marginRight: 5 }}>
              {item.icon}
            </div>
            <div style={{ height: 'max-content', color: '#435fff', textAlign: "left" }}>
              <div style={{ fontWeight: 600, fontSize: 20 }}>{item.title}</div>
              <div style={{ fontSize: 12, marginLeft: 3, marginTop: 15 }}>{item.text}</div>
            </div>
            <div className={style.cardHoverStyle}>
              <PlusCircleFilled />
            </div>
          </div>
        ))}
      </div>
      <div className={style.workHistoryBox}>
        <div className={style.workListTop}>
          <div style={{ width: 160 }}>
            <Segmented
              style={{ boxShadow: '0px 0px 4px #00000052' }}
              block={true}
              options={segmentItem}
              value={currentValue}
              onChange={value => {
                handleChangeType(value)
              }} />
          </div>
          <div style={{ flex: 1 }}></div>
          <div style={{ width: 'max-content' }}>
            <Input
              placeholder='输入作业名称'
              style={{ width: 200, height: 40, fontFamily: 'siyuan', boxShadow: '0px 0px 2px #0000001d' }}
              suffix={<SearchOutlined style={{ fontSize: 18 }} />}
            />

          </div>
        </div>
        {listType === '预习任务' && <PreHistory onPublish={showPublishModal} reload={reload} />}
        {listType === '课后习题' && <HomeWorkHistory onPublish={showPublishHomework} reload={reload} />}
        {listType === '考试' && <ExamHistory onPublish={showPublishHomework} />}
      </div>


      {/* 弹窗 */}
      <Modal
        style={{ top: 50 }}
        open={open}
        maskClosable={true}
        destroyOnClose={true}
        cancelButtonProps={{ style: { display: 'none' } }}
        okButtonProps={{ style: { display: 'none' } }}
        onCancel={handleCancel}
      >
        {isLoading ?
          <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
            <ModalLoadingComponent />
            <div style={{ fontSize: 20, fontFamily: 'youshe', marginTop: 30 }}>正在创建中，请稍等</div>
          </div>
          :
          <div style={{ width: 470, height: 600, display: 'flex', alignItems: 'center', flexDirection: 'column', fontWeight: 'bolder' }}>
            <div style={{ fontSize: 22, width: '80%', textAlign: 'center', paddingBottom: 10, fontWeight: 600, marginBottom: 20, borderBottom: '2px solid black' }}>{modalTitle}</div>
            {isShowWork ?
              <WorkSelectForm onSubmit={createHomework} allData={knowledgeData} /> :
              <PreviewSelectForm onSubmit={createPreview} allData={knowledgeData} />}
          </div>
        }
      </Modal>

      <Modal
        width={400}
        open={sendOpen}
        destroyOnClose={true}
        cancelButtonProps={{ style: { display: 'none' } }}
        okButtonProps={{ style: { display: 'none' } }}
        onCancel={handlePublishCancel}
      >
        <div
          style={{ fontSize: 22, width: '80%', textAlign: 'center', paddingBottom: 10, fontWeight: 600, margin: 'auto', marginBottom: 10 }}
        >发布作业</div>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: 300, margin: 'auto' }}>
          {wherePublish === 0 && <PublishForm onSubmit={publishPre} onSubmitWork={publishPre} classList={classList} hid={hid} />}
          {wherePublish === 1 && <PublishHomework onSubmitWork={publishWork} classList={classList} hid={hid} />}


        </div>
      </Modal>

    </ConfigProvider>
  )
}
export default HomeworkEntry