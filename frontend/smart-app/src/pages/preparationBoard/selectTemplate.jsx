import Icon, { LoadingOutlined } from '@ant-design/icons';
import { Button, Col, Form, Input, Row, Select, Spin } from 'antd'
import { useEffect, useState } from 'react';
import { data, useNavigate } from 'react-router-dom';
import style from './preparationBoard.module.css'
import TemplateEditor from '../../component/preparation/templateEditor';
import ModalComponent from '@/component/preparation/modalComponent';
import ModalLoadingComponent from '../../component/Loading/modalLoading'
import { postCreateNewDesign, postPushSyllabus } from '@/apis/preparation'



const EntryComponent = ({ onConfirm }) => {
  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm()
    }
  }



  return (
    <div style={{ width: 'max-content', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
      <button
        htmlType="submit"
        className={style.shengchengBtn}
        onClick={() => handleConfirm()}
      >
        进入编辑
      </button>
    </div>)
}

const SelectTemplate = ({ templateData }) => {
  const navigate = useNavigate()


  const [showContent, setShowContent] = useState(true)
  const [currentShow, setCurrentShow] = useState(1)
  const [tdId, setTdId] = useState()
  const uid = sessionStorage.getItem('uid')

  const initFormData = {
    ptitle: [templateData.topTitle, templateData.secondaryTitle],
    subject: templateData.type
  }

  //新建教学设计事件
  async function handleSubmit(value) {
    let { data } = await postCreateNewDesign(value)
    setTdId(data)
    const prop = {
      authorId: uid,
      content: templateData.content,
      syllabusId: templateData.syllabusId,

    }
    setCurrentShow(2)
    setTimeout(() => {
      postPushSyllabus(prop, data)
      setShowContent(true)
      setCurrentShow(3)
    }, 1000);
  }

  //根据大纲进入编辑器
  function entryPreparation() {
    setCurrentShow(2)
    console.log(tdId)
    setTimeout(() => {
      navigate(`/texteditor/${tdId}`)
    }, 1000)
  }



  // 1 显示表格，2 显示加载动画，3 显示按钮
  function showComponent(e) {
    switch (e) {
      case 1:
        return <ModalComponent onSubmit={handleSubmit} initFormData={initFormData} />;
      case 2:
        return <ModalLoadingComponent />
      case 3:
        return <EntryComponent onConfirm={entryPreparation} />
      default:
        break;
    }
  }

  return (


    <div>

      <div style={{ color: '#000', lineHeight: 1, textAlign: 'center', fontSize: 20, fontWeight: 600 }}>大纲信息</div>
      <div className={style.aipreStyle}>
        {showContent && <div className={style.aipreContent} ><TemplateEditor returnHtml={templateData.content} /></div>}

        <div className={style.aipreForm} style={{ boxShadow: 'none' }}>
          {showComponent(currentShow)}
        </div>
      </div>


    </div>


  )
}

export default SelectTemplate