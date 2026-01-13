import { Col, Form, Input, message, Row, Cascader } from 'antd'
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom'
import style from './preparationBoard.module.css'
import LittleEditor from '@/component/preparation/littleEditor'
import ModalLoadingComponent from '@/component/Loading/modalLoading'
import { postAIGenerate, postSaveSyllabus } from '@/apis/preparation'
import { getSubjectList } from '../../apis/homeworkAPI'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const ConfirmForm = ({ onSave, content, topTitle, secondaryTitle }) => {
  const [form] = Form.useForm()
  const handleSubmit = async (values) => {
    // 转换Markdown
    const dirtyHtml = await marked.parse(content)
    const cleanHtml = DOMPurify.sanitize(dirtyHtml)
    const prop = {
      ...values,
      content: cleanHtml,
      authorId: 12,
      topTitle: topTitle,
      secondaryTitle: secondaryTitle
    }
    console.log(prop)
    if (onSave) {
      onSave(prop)
    }
  }


  return (
    <Form
      onFinish={handleSubmit}
      layout="vertical"
      labelCol={{ flex: '30px' }}
      labelAlign="left"
      wrapperCol={{ flex: 0.5 }}
      colon={false}
      style={{ width: 240 }}
      form={form}

    >
      <Form.Item label="大纲名称" name="name" rules={[{ required: true, message: '名称未填写！' }]}>
        <Input
          placeholder="请输入大纲名称"
        />
      </Form.Item>

      <Form.Item >
        <div style={{ textAlign: 'center' }}>
          <button
            htmlType="submit"
            className={style.shengchengBtn}
          >
            保存大纲
          </button>
        </div>
      </Form.Item>
    </Form>
  )
}

const FormComponent = ({ onSubmit }) => {
  let { tsId, schoolId } = useParams()
  const [knowledgeData, setKnowledgeData] = useState([])
  const [form] = Form.useForm()


  const getSubjectItem = async () => {
    let { data } = await getSubjectList(tsId, schoolId)
    setKnowledgeData(data)
  }

  useEffect(() => {
    getSubjectItem()
  }, [])


  const handleSubmit = (values) => {

    const prop = {
      extraRequirements: values.extraRequirements,
      extraRestrictions: values.extraRestrictions,
      topTitle: values.ptitle[0],
      secondaryTitle: values.ptitle[1],
      authorId: 12
    }
    if (onSubmit) {
      onSubmit(prop)
    }
  }


  return (
    <Form
      onFinish={handleSubmit}
      layout="vertical"
      labelCol={{ flex: '30px' }}
      labelAlign="left"
      wrapperCol={{ flex: 0.5 }}
      colon={false}
      style={{ width: 240 }}
      form={form}

    >
      {/* 知识点 */}
      <Form.Item label="知识点" name="ptitle" rules={[{ required: true, message: '知识点未选择！' }]}>

        <Cascader
          fieldNames={{ label: 'title', value: 'title', children: 'children' }}
          options={knowledgeData}
          placeholder="请选择知识点"
        />
      </Form.Item>


      <Form.Item label="额外说明" name="extraRequirements" >
        <Input />
      </Form.Item>

      <Form.Item label="条件限制" name="extraRestrictions">
        <Input />
      </Form.Item>

      <Form.Item >
        <div style={{ textAlign: 'center' }}>
          <button
            htmlType="submit"
            className={style.shengchengBtn}
          >
            点击生成
          </button>
        </div>
      </Form.Item>


    </Form>
  )
}

const GenerateComponent = ({ onConfirm, onGenerateAgain }) => {
  const handleConfirm = () => {
    if (onConfirm) { onConfirm() }

  }

  const handleAgain = () => {
    if (onGenerateAgain) { onGenerateAgain() }
  }

  return (
    <div style={{ width: 'max-content', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ height: 80, fontFamily: 'siyuan' }}>选择下一步操作</div>
      <button
        htmlType="submit"
        className={style.againBtn}
        onClick={() => handleAgain()}
        style={{ marginBottom: 20 }}
      >
        重新生成
      </button>
      <button
        htmlType="submit"
        className={style.shengchengBtn}
        onClick={() => handleConfirm()}
      >
        确认
      </button>
    </div>)
}

const AIpreparation = () => {

  const navigate = useNavigate()
  const [showContent, setShowContent] = useState(false)
  const [currentShow, setCurrentShow] = useState(1)
  const [generateContent, setGenerateContent] = useState('')
  let { tsId, schoolId } = useParams()
  const [topTitle, setTopTitle] = useState()
  const [secondaryTitle, setSecondaryTitle] = useState()


  //发送生成大纲请求
  async function handleSubmit(prop) {
    setCurrentShow(2)
    let { data } = await postAIGenerate(prop)
    setTopTitle(data.designTitle)
    setSecondaryTitle(data.secondaryTitle)
    setGenerateContent(data.content)
    setTimeout(() => {
      setShowContent(true)
      setCurrentShow(3)
    }, 1000)
  }

  //再次生成事件
  function generateAgain() {
    setCurrentShow(1)
  }

  //保存大纲事件
  async function handleSave(prop) {
    let { data } = await postSaveSyllabus(prop)
    console.log(data)
    message.success('保存成功')
    setTimeout(() => {
      navigate(`/home/template/${tsId}/${schoolId}`)
    }, 500);
  }

  // 确认选择大纲事件
  function handleConfirm() {
    setCurrentShow(4)

  }

  // 1 显示表格，2 显示加载动画，3 显示按钮
  function showComponent(e) {
    switch (e) {
      case 1:
        return <FormComponent onSubmit={handleSubmit} />
      case 2:
        return <ModalLoadingComponent />;
      case 3:
        return <GenerateComponent onGenerateAgain={generateAgain} onConfirm={handleConfirm} />;
      case 4:
        return <ConfirmForm onSave={handleSave} content={generateContent} topTitle={topTitle} secondaryTitle={secondaryTitle} />
      default:
        break;
    }
  }



  return (


    <div>

      <div style={{ color: '#000', lineHeight: 1, textAlign: 'center', fontSize: 20, fontWeight: 600 }}>AI生成大纲</div>
      <div className={style.aipreStyle}>
        {showContent && <div className={style.aipreContent}><LittleEditor returnHtml={generateContent} /></div>}

        <div className={style.aipreForm}>
          {showComponent(currentShow)}
        </div>
      </div>


    </div>


  )
}

export default AIpreparation