import { Avatar, Button, Collapse, ConfigProvider, Divider, Input, Tag } from 'antd'
import style from './homeWork.module.css'
import { use, useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom';
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeftOutlined, RetweetOutlined } from '@ant-design/icons'
import EditableText from '@/component/homeWork/EditableText.jsx'
import InfoIcon from '@/assets/svg/搜索.svg'
import PreResource from '@/assets/svg/凭证.svg'
import ResourceEditor from '@/component/homeWork/resourceEditor.jsx'
import { getPreContent, postSavePreview } from '@/apis/homeworkAPI'
import PageLoadingComponent from '../../component/Loading/pageLoadingComponent'


//导航按钮聚焦
const btnFocus = {
  background: 'linear-gradient(120deg, #54a2fc 0%, #445FFF 100%)',
  color: '#ffffff',
  boxShadow: '0px 0px 4px #03030361'
}

const btnNone = {
  background: 'linear-gradient(120deg, #54a2fc60 0%, #4460ff60 100%)'

}

//默认选项样式
const defaultOption = {
  width: 25,
  height: 25,
  fontSize: 14,
  border: 'none',
  fontWeight: 600,
  borderRadius: '50%',
  padding: 5,
  background: '#e4e4e4',
  color: '#525252'
}

//正确选项样式
const correctOption = {
  width: 25,
  height: 25,
  fontSize: 14,
  fontWeight: 600,
  borderRadius: '50%',
  padding: 5,
  background: '#fff',
  color: '#435fff',
  border: '1px solid #435fff'
}

const PreviewWorkPage = () => {
  let { p_id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()


  const [ptId, setPtId] = useState()
  const [preName, setPreName] = useState('')
  const [totalScore, setTotalScore] = useState('')
  const [ptitle, setPTitle] = useState('')
  const [secondaryTitle, setSecondaryTitle] = useState('')
  const [renderItem, setRenderItem] = useState({})
  const [showResource, setShowResource] = useState(true)
  const [resourceContent, setResourceContent] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const userToken = localStorage.getItem('authToken')

  //题目分组
  const [singleList, setSingleList] = useState([])
  const [multipleList, setMultipleList] = useState([])
  const [blankList, setBlankList] = useState([])
  const [replyList, setReplyList] = useState([])
  //用于标记题型
  const [currentType, setCurrentType] = useState('')
  const [currentIndex, setCurrentIndex] = useState(-1)

  //数据请求
  useEffect(() => {
    if (p_id === '0') {
      getAuditContent()
    } else {
      getHistoryContent(p_id)
    }
  }, [])

  const getHistoryContent = async (pId) => {
    setIsLoading(true)
    let { data } = await getPreContent(pId)
    console.log(data)
    setPtId(data.ptId)
    setTotalScore(data.totalScore)
    setPTitle(data.ptitle)
    setSecondaryTitle(data.secondaryTitle)
    setPreName(data.previewName)
    setResourceContent(data.previewText.readResource)
    devideArr(data.taskList)
    setIsLoading(false)
  }

  const getAuditContent = () => {
    const data = location.state?.content
    if (!data) {
      navigate('/404')
    } else {
      setTotalScore(data.totalScore)
      setPtId(data.ptId)
      setPTitle(data.ptitle)
      setSecondaryTitle(data.secondaryTitle)
      setPreName(data.previewName)
      setResourceContent(data.previewText.readResource)
      devideArr(data.taskList)
    }
  }

  //修改解析
  const reviseMethod = (qid, newValue) => {
    setSingleList(prevList =>
      prevList.map(item =>
        item.qid === qid
          ? { ...item, answerAnalysis: newValue }
          : item
      )
    )
  }

  //修改题目
  const reviseContent = (qid, newValue) => {
    setSingleList(prevList =>
      prevList.map(item =>
        item.qid === qid
          ? { ...item, qcontent: newValue }
          : item
      )
    )
  }

  //修改答案
  const reviseAnswer = (qid, newValue) => {
    setSingleList(prevList =>
      prevList.map(item =>
        item.qid === qid
          ? { ...item, correctAnswer: newValue }
          : item
      )
    )
  }

  const reviseOption = (qid, newValue) => {
    setSingleList(prevList =>
      prevList.map(item =>
        item.qid === qid
          ? { ...item, answer_analysis: newValue }
          : item
      )
    )
  }

  //该hook用于对题目进行分组
  const devideArr = (list) => {
    const single = []
    const multiple = []
    const blank = []
    const reply = []

    list?.forEach(item => {
      if (item.qtype === '单选题') {
        single.push(item)
      } else if (item.qtype === '多选题') {
        multiple.push(item)
      } else if (item.qtype === '填空题') {
        blank.push(item)
      } else if (item.qtype === '简答题') {
        reply.push(item)
      }
    })
    setSingleList(single)
    setMultipleList(multiple)
    setBlankList(blank)
    setReplyList(reply)
    setRenderItem(list[0])
  }

  //变换RenderItem
  function typeSelection(type, index) {
    if (type === '单选题') {
      setRenderItem(singleList[index])
    } else if (type === '多选题') {
      setRenderItem(multipleList[index])
    } else if (type === '填空题') {
      setRenderItem(blankList[index])
    } else if (type === '简答题') {
      setRenderItem(replyList[index])
    }
  }


  //选项名字转换
  function selectionsChange(e) {
    switch (e) {
      case 0:
        return 'A '
      case 1:
        return 'B '
      case 2:
        return 'C '
      case 3:
        return 'D '
      default:
        break;
    }
  }
  //题目数量
  function exersiceCouts(e) {
    switch (e) {
      case '单选题': if (singleList)
        return singleList.length
      case '多选题': if (multipleList)
        return multipleList.length
      case '填空题': if (blankList)
        return blankList.length
      case '简答题': if (replyList)
        return replyList.length
      default:
        break;
    }
  }

  const calculateTotal = (arr) => {
    return arr.reduce((sum, item) => sum + (item.defaultScore || 0), 0);
  }

  //计算题型总分
  function typeTotalScore(e) {
    switch (e) {
      case '单选题': if (singleList)
        return calculateTotal(singleList)
      case '多选题': if (multipleList)
        return calculateTotal(multipleList)
      case '填空题': if (blankList)
        return calculateTotal(blankList)
      case '简答题': if (replyList)
        return calculateTotal(replyList)
      default:
        break;
    }
  }


  //导航栏按钮点击事件
  const handleBtnClick = (type, e) => {
    setShowResource(false)
    setCurrentType(type)
    setCurrentIndex(e)
    setTimeout(() => {
      typeSelection(type, e)
    }, 50);
  }

  //预习资料按钮
  const handleResourceClick = () => {
    setShowResource(true)
    setCurrentIndex(-1)
  }


  //保存预习任务事件
  const handleSavePreview = async () => {
    const combinedTasks = [
      ...singleList,
      ...multipleList,
      ...blankList,
      ...replyList
    ]
    const prop = {
      previewText: { question: [], readResource: resourceContent },
      taskList: combinedTasks,
      ptId: ptId
    }
    console.log(prop)
    await postSavePreview(prop)
    //还未测试过此处跳转
    navigate(-1)
  }

  //生成Word事件
  const handleGenerateWord = async (ptId, token) => {
    try {
      // 从路径中提取文件名
      // 构建请求URL
      const url = `http://localhost:8080/homeworkDetails/export?ptId=${ptId}`
      // 发起请求
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'token': token,
          'Accept': 'application/octet-stream'
        }
      });
      // 检查响应状态
      if (!response.ok) {
        throw new Error(`服务器返回错误: ${response.status} ${response.statusText}`)
      }

      // 获取文件数据
      const blob = await response.blob()
      // 创建下载链接
      const downloadUrl = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = `${preName}.docx`
      link.style.display = 'none'

      // 触发下载
      document.body.appendChild(link)
      link.click()

      // 清理资源
      document.body.removeChild(link)
      setTimeout(() => URL.revokeObjectURL(downloadUrl), 100)

      return true
    } catch (error) {
      console.error('文件下载失败:', error)
      throw new Error(`下载失败: ${error.message}`)
    }
  }

  return (
    <ConfigProvider>
      {isLoading ? <PageLoadingComponent /> : <>
        <div className={style.workContent}>
          <div className={style.header}>
            <div style={{ width: '85%', margin: 'auto', display: 'flex', alignItems: 'center' }}>
              <button className={style.defBtn} onClick={() => navigate(-1)} style={{ width: 40, height: 40, fontSize: 18 }} ><ArrowLeftOutlined /></button>
              {/* <span className={style.workTag} style={{height:28}}>进行中</span> */}
              <span className={style.workName}>{preName}<span>（{totalScore}分）</span></span>
              <div style={{ flex: 1 }}></div>
              <>
                {p_id === '0' && <div className={style.saveBtn} onClick={() => handleSavePreview()}>保存</div>}
                {p_id !== '0' && <div className={style.saveBtn} onClick={() => handleGenerateWord(p_id, userToken)}>生成Word</div>}
              </>
            </div>
          </div>
          <div style={{ display: 'flex', width: '90%', minHeight: 0, flex: 1, margin: 'auto' }}>
            <div className={style.workLeft}>
              <div className={style.workInfo}>
                <div className={style.glassEffect}>
                  <div style={{ fontSize: 18, fontWeight: 600, color: '#ffffff', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{ptitle}</div>
                  <div
                    style={{ fontSize: 14, fontFamily: 'siyuan', color: '#ececec', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {secondaryTitle}</div>
                </div>
                <img src={InfoIcon} alt='#' style={{ width: 86 }} />
              </div>
              <div
                style={{ width: '80%', height: 80, background: '#fff', margin: 'auto', boxShadow: '0px 0px 4px #0000003a', display: 'flex', alignItems: 'center', padding: 15 }}>
                <Avatar src={PreResource} size={64} />
                <button
                  style={currentIndex === -1 ? btnFocus : btnNone}
                  className={style.resourceBtn}
                  onClick={() => handleResourceClick()}>预习资料</button>
              </div>
              <div className={style.exerciseBtnBox}>
                <div style={{ width: '100%', fontFamily: 'siyuan', lineHeight: 1 }}>
                  {singleList.length !== 0 && (<div style={{ marginBottom: 10 }}>单选题</div>)}
                  {singleList?.map((item, index) => (
                    <button
                      style={currentIndex === index && currentType === '单选题' ? btnFocus : btnNone}
                      className={style.exerciseBtn} key={item.qid}
                      onClick={() => handleBtnClick(item.qtype, index)}
                    >
                      {index + 1}
                    </button>))}

                  {multipleList.length !== 0 && (<>
                    <div style={{ height: 15, width: '100%', borderTop: '1px solid #eaeaea', marginTop: 5 }} />
                    <div style={{ marginBottom: 10 }}>多选题</div>
                  </>)}
                  {multipleList?.map((item, index) => (
                    <button
                      style={currentIndex === index && currentType === '多选题' ? btnFocus : btnNone} className={style.exerciseBtn} key={item.qid}
                      onClick={() => handleBtnClick(item.qtype, index)}
                    >
                      {index + 1}
                    </button>))}

                  {blankList.length !== 0 && (<>
                    <div style={{ height: 15, width: '100%', borderTop: '1px solid #eaeaea', marginTop: 5 }} />
                    <div style={{ marginBottom: 10 }}>填空题</div>
                  </>)}
                  {blankList?.map((item, index) => (
                    <button
                      style={currentIndex === index && currentType === '填空题' ? btnFocus : btnNone} className={style.exerciseBtn} key={item.qid}
                      onClick={() => handleBtnClick(item.qtype, index)}
                    >
                      {index + 1}
                    </button>))}

                  {replyList.length !== 0 && (<>
                    <div style={{ height: 15, width: '100%', borderTop: '1px solid #eaeaea', marginTop: 5 }} />
                    <div style={{ marginBottom: 10 }}>简答题</div>
                  </>)}
                  {replyList?.map((item, index) => (
                    <button
                      style={currentIndex === index && currentType === '简答题' ? btnFocus : btnNone} className={style.exerciseBtn} key={item.qid}
                      onClick={() => handleBtnClick(item.qtype, index)}
                    >
                      {index + 1}
                    </button>))}
                </div>

              </div>
            </div>

            <div className={style.workRight}>
              {showResource ?
                <div className={style.problemContent} style={{ paddingTop: 0 }}>
                  <ResourceEditor content={resourceContent} />
                </div>
                :
                <div className={style.problemContent}>
                  <div style={{ fontSize: 20, fontWeight: 600, marginBottom: 30 }}>{renderItem.qtype}<span style={{ fontSize: 14, color: '#505050' }}>（共{exersiceCouts(renderItem.qtype)}题，{typeTotalScore(renderItem.qtype)}分）</span></div>
                  <div style={{ fontSize: 18, marginBottom: 36 }}>
                    <Tag color='geekblue' bordered={false} style={{ fontFamily: 'siyuan', fontSize: 14, marginRight: 14 }}>{renderItem.defaultScore}分</Tag>
                    <span>{currentIndex + 1}、</span><EditableText key={renderItem.qid} value={renderItem.qcontent} onChange={(newVal) => reviseContent(renderItem.qid, newVal)} />
                  </div>
                  <div style={{ marginLeft: 64 }}>
                    {renderItem.options?.map((item, index) => (
                      <div style={{ fontSize: 18, marginBottom: 25 }} key={index}><button style={selectionsChange(index) === renderItem.correctAnswer ? correctOption : defaultOption}>{selectionsChange(index)}</button> <span>{item}</span></div>
                    ))}
                  </div>
                  <Divider></Divider>
                  <div style={{ width: '100%', height: 'max-content', background: '#F2F5FF', paddingInline: 20, paddingTop: 20, paddingBottom: 30 }}>
                    <div style={{ fontSize: '1em', marginBottom: 32 }}><span style={{ marginRight: 10 }}>【答案】</span><EditableText key={renderItem.qid} value={renderItem.correctAnswer} onChange={(newVal) => reviseAnswer(renderItem.qid, newVal)} /></div>
                    <div style={{ fontSize: 16 }}><span style={{ marginRight: 10 }}>【解析】</span><EditableText key={renderItem.qid} value={renderItem.answerAnalysis} onChange={(newVal) => reviseMethod(renderItem.qid, newVal)} /></div>
                  </div>
                </div>
              }


            </div>

          </div>
        </div>
      </>
      }
    </ConfigProvider>
  )
}


export default PreviewWorkPage