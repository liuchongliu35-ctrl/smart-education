import './style.css' // 引入 css
import React, { useState, useEffect } from 'react'
import { Editor, Toolbar } from '@wangeditor/editor-for-react'
import { Boot } from '@wangeditor/editor'
import style from '@/pages/TextEditor/textEditor.module.css'
import { useNavigate, useParams } from 'react-router-dom'
import Icon, { HomeOutlined, FormOutlined, CaretDownOutlined, CalendarOutlined } from '@ant-design/icons';
import { ConfigProvider, Layout, Divider, message } from 'antd'
import Example from '@/assets/png/合照.png'
import WordComponent from './wordComponent'
import VideoComponent from './videoComponent'
import ImgComponent from './imgComponent'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import 'katex/dist/katex.min.css'
import testMarkdown from './nice.md?raw'
import { getHistoryDesignContent, postEditorCache } from '../../apis/preparation'
import PageLoadingComponent from '../Loading/pageLoadingComponent'

const btnItem = [
  {
    key: '1',
    icon: <HomeOutlined />,
    label: '文案'
  },
  {
    key: '2',
    icon: <FormOutlined />,
    label: '图片'
  }, {
    key: '3',
    icon: <CalendarOutlined />,
    label: '视频'
  }
]






const TextEditor = () => {

  class WorkBtnMenu {
    constructor() {
      this.title = '文案优化' // 自定义菜单标题
      this.iconSvg = '<svg t="1744036470543" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3030" width="32" height="32"><path d="M998.4 409.6l-61.44-20.48c-15.36-5.12-25.6-15.36-30.72-30.72l-25.6-76.8c-5.12-10.24-15.36-10.24-20.48 0L834.56 358.4c-5.12 15.36-15.36 25.6-25.6 25.6l-66.56 25.6c-10.24 5.12-10.24 15.36 0 20.48l66.56 20.48c10.24 5.12 20.48 15.36 25.6 25.6l25.6 61.44c5.12 10.24 15.36 10.24 20.48 0l25.6-61.44c5.12-10.24 15.36-20.48 25.6-25.6l66.56-20.48c5.12-5.12 5.12-15.36 0-20.48zM332.8 609.28l-25.6-10.24c-15.36-5.12-20.48-15.36-25.6-25.6l-10.24-35.84c-5.12-10.24-15.36-10.24-20.48 0l-10.24 35.84c-5.12 15.36-15.36 25.6-25.6 25.6l-25.6 10.24c-10.24 5.12-10.24 15.36 0 20.48l25.6 10.24c10.24 5.12 20.48 15.36 25.6 25.6l10.24 25.6c5.12 10.24 15.36 10.24 20.48 0l5.12-25.6c5.12-10.24 15.36-20.48 25.6-25.6l25.6-10.24c10.24 0 10.24-15.36 5.12-20.48z" fill="#539CFC" p-id="3031"></path><path d="M819.2 204.8c30.72 0 51.2-20.48 51.2-51.2s-20.48-51.2-51.2-51.2H204.8c-30.72 0-51.2 20.48-51.2 51.2s20.48 51.2 51.2 51.2h256v634.88c0 30.72 20.48 51.2 51.2 51.2s51.2-20.48 51.2-51.2V204.8h256z" fill="#539CFC" p-id="3032"></path></svg>'
      this.tag = 'button'
      this.setSelectText = setSelectText
      this.setCurrentBtnKey = setCurrentBtnKey
    }

    getValue(editor) {
      return ' hello '
    }
    isActive(editor) {
      return false
    }
    isDisabled(editor) {
      return false
    }

    exec(editor) {
      if (this.isDisabled(editor))
        return
      const selected = editor.getSelectionText()
      this.setSelectText(selected)
      this.setCurrentBtnKey('1')
      console.log(selected)
    }
  }

  class ImgBtnMenu {
    constructor() {
      this.title = '图片生成' // 自定义菜单标题
      this.iconSvg = '<svg t="1744036546602" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4439" data-spm-anchor-id="a313x.search_index.0.i5.1ed43a813wSpsf" width="32" height="32"><path d="M859.802 102.4l-48.333 88.269-88.218 48.281 88.218 48.128 48.333 88.423 48.281-88.371 88.269-48.282-88.269-48.18-48.281-88.268zM624.486 401.971l-51.507 28.16 51.507 28.263 28.16 51.456 28.212-51.456 51.456-28.212-51.456-28.16-28.16-51.507-28.212 51.456z" p-id="4440"></path><path d="M915.558 430.182a43.827 43.827 0 0 0-43.776 43.776v257.024c0 30.772-17.408 58.88-44.953 72.704-0.768-1.126-1.536-2.406-2.56-3.635L686.592 633.088a46.694 46.694 0 0 0-71.987 0L553.37 707.43 370.278 485.683a46.694 46.694 0 0 0-71.987 0L138.957 678.861V304.794a81.562 81.562 0 0 1 81.408-81.767H608.87a43.776 43.776 0 0 0 43.776-43.827v-7.936a43.776 43.776 0 0 0-43.776-43.827H227.43A176.18 176.18 0 0 0 51.2 303.667v428.288a176.23 176.23 0 0 0 176.23 176.23h555.776a176.23 176.23 0 0 0 176.18-176.23V473.907a43.827 43.827 0 0 0-43.776-43.878v0.153z" p-id="4441" data-spm-anchor-id="a313x.search_index.0.i4.1ed43a813wSpsf" class="selected" fill="#539CFC"></path></svg>'
      this.tag = 'button'
      this.setCurrentBtnKey = setCurrentBtnKey
      this.setSelectImgText = setSelectImgText
    }

    getValue(editor) {
      return ' hello '
    }
    isActive(editor) {
      return false
    }
    isDisabled(editor) {
      return false
    }

    exec(editor) {
      if (this.isDisabled(editor)) return
      const selectedText = editor.getSelectionText()
      this.setSelectImgText(selectedText)
      this.setCurrentBtnKey('2')

    }
  }

  class VideoBtnMenu {
    constructor() {
      this.title = '视频生成'
      this.iconSvg = '<svg t="1744036828950" class="icon" viewBox="0 0 1365 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6522" width="32" height="32"><path d="M256.006827 42.669796a42.666382 42.666382 0 1 1 85.332764 0v938.660408a42.666382 42.666382 0 1 1-85.332764 0zM554.671502 383.147526c0-23.039846 16.213225-33.279778 37.546417-23.039847l227.838481 114.345905c41.813055 21.333191 40.959727 55.466297 0 75.94616l-33.279778 17.066553-193.705376 96.426024c-21.333191 10.239932-38.399744 0-38.399744-23.039847z" fill="#333333" p-id="6523"></path><path d="M0.008533 384.000853A41.813055 41.813055 0 0 1 41.821588 341.334471h255.998293a42.666382 42.666382 0 0 1 43.51971 42.666382 41.813055 41.813055 0 0 1-41.813054 42.666383h-255.998294A42.666382 42.666382 0 0 1 0.008533 384.000853zM0.008533 639.999147A41.813055 41.813055 0 0 1 41.821588 597.332764h255.998293a42.666382 42.666382 0 0 1 43.51971 42.666383 41.813055 41.813055 0 0 1-41.813054 42.666382h-255.998294A42.666382 42.666382 0 0 1 0.008533 639.999147zM1024.001707 384.000853a41.813055 41.813055 0 0 1 41.813054-42.666382h255.998294a42.666382 42.666382 0 0 1 41.813054 42.666382 41.813055 41.813055 0 0 1-41.813054 42.666383h-255.998294a42.666382 42.666382 0 0 1-41.813054-42.666383zM1024.001707 639.999147a41.813055 41.813055 0 0 1 41.813054-42.666383h255.998294a42.666382 42.666382 0 0 1 41.813054 42.666383 41.813055 41.813055 0 0 1-41.813054 42.666382h-255.998294a42.666382 42.666382 0 0 1-41.813054-42.666382z" fill="#333333" p-id="6524"></path><path d="M1024.001707 42.669796a42.666382 42.666382 0 1 1 85.332764 0v938.660408a42.666382 42.666382 0 1 1-85.332764 0z" fill="#333333" p-id="6525"></path><path d="M1262.933447 0.003413H102.407851A101.54599 101.54599 0 0 0 0.008533 101.549403v820.047866A103.252645 103.252645 0 0 0 102.407851 1023.996587h1160.525596a101.54599 101.54599 0 0 0 102.399318-101.54599V101.549403A103.252645 103.252645 0 0 0 1262.933447 0.003413z m17.066553 870.394198a68.266212 68.266212 0 0 1-67.412884 68.266211H152.754182a68.266212 68.266212 0 0 1-67.412884-68.266211V153.602389a68.266212 68.266212 0 0 1 67.412884-68.266211h1059.832934a68.266212 68.266212 0 0 1 67.412884 68.266211z" fill="#333333" p-id="6526"></path></svg>'
      this.tag = 'button'
      this.setCurrentBtnKey = setCurrentBtnKey
      this.setSelectVideoText = setSelectVideoText
    }

    getValue(editor) {
      return ' hello '
    }
    isActive(editor) {
      return false
    }
    isDisabled(editor) {
      return false
    }

    exec(editor) {
      if (this.isDisabled(editor)) return
      const selectedText = editor.getSelectionText()
      this.setSelectVideoText(selectedText)
      this.setCurrentBtnKey('3')
    }
  }


  const [editor, setEditor] = useState(null)
  const [currentBtnKey, setCurrentBtnKey] = useState('1')
  const [designContent, setDesignContent] = useState('第一章：空间几何体')
  const [designName, setDesignName] = useState('')
  const [selectText, setSelectText] = useState('')
  const [selectImgText, setSelectImgText] = useState('')
  const [selectVideoText, setSelectVideoText] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [reload, setReload] = useState(false)
  const [messageApi, contextHolder] = message.useMessage()
  const [lastContent, setLastContent] = useState('')
  const uid = sessionStorage.getItem('uid')

  const navigate = useNavigate()
  let { design_id } = useParams()

  /* ——————数据请求———————— */
  const getDesignList = async () => {
    setIsLoading(true)
    let { data } = await getHistoryDesignContent(design_id)
    setDesignName(data.designName)
    setTimeout(() => {
      setDesignContent(data.content)
      setIsLoading(false)
      try {
        Boot.registerMenu(menu1Conf)
        Boot.registerMenu(imgBtn)
        Boot.registerMenu(videoBtn)
      } catch (e) { }
    }, 800)
  }

  useEffect(() => {
    getDesignList()
  }, [])

  const menu1Conf = {
    key: 'print',
    factory() {
      return new WorkBtnMenu()
    }
  }
  const videoBtn = {
    key: 'videoai',
    factory() {
      return new VideoBtnMenu()
    }
  }

  const imgBtn = {
    key: 'imgai',
    factory() {
      return new ImgBtnMenu()
    }
  }

  //自定义工具栏配置
  const toolbarConfig = {
    toolbarKeys: [
      'undo', 'redo',          // 撤销/重做
      'clearStyle',            //格式清除
      'emotion',
      '|',
      'headerSelect',         // 标题选择
      'fontSize',
      'bold', 'italic', 'underline',  // 基础样式
      {
        key: 'group-more-style', // 对齐方式组
        title: '更多',
        menuKeys: ['through', 'sup', 'sub', 'code']
      },
      '|',                    // 分隔符
      'color',
      'bgColor',
      '|',
      {
        key: 'group-justify', // 对齐方式组
        title: '对齐',
        menuKeys: ['justifyLeft', 'justifyRight', 'justifyCenter']
      },
      'lineHeight',
      "bulletedList",
      "numberedList",
      "indent",
      "delIndent",
      'uploadImage',          // 插入图片 
      'blockquote',
      'divider',
      "insertTable"
    ]
  }

  // 编辑器配置
  const editorConfig = {

    placeholder: '请输入内容...',
    hoverbarKeys: {
      text: {
        menuKeys: ['print', 'imgai', 'videoai']
      }
    },
    MENU_CONF: {
      uploadImage: {
        server: '', //上传后端地址
        customUpload: (file, insertFn) => {
          // 创建文件阅读器
          const reader = new FileReader()
          //读取文件内容
          reader.readAsDataURL(file)// 把图片转成Base64格式（一串特殊字符）
          //读取完成后
          reader.onload = () => {
            insertFn(reader.result, '本地图片')//插入图片
          }

          reader.onerror = () => {
            alert('图片加载失败')
          }
        },
        allowedFileTypes: ['image/*'],
        maxFileSize: 2 * 1024 * 1024 // 2MB
      }
    },
  }

  // 及时销毁 editor ，重要！
  useEffect(() => {
    return () => {
      if (editor == null) return
      editor.destroy()
      setEditor(null)
    }
  }, [editor])

  const handleSave = async (content, uid) => {
    const prop = {
      tdId: design_id,
      uid: uid,
      newContents: content
    }
    console.log(prop)
    await postEditorCache(prop)
    setReload(pre => !pre)
    messageApi.open({
      type: 'success',
      content: '保存成功',
    })
  }


  return (

    <ConfigProvider wave={{ disabled: true }}>
      {isLoading ? <PageLoadingComponent /> : <>
        {contextHolder}
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', border: '1px solid #eaeaea' }}>
          <div className={style.header}>
            <button className={style.defBtn} onClick={() => navigate(-1)} style={{ width: 50, height: 50 }} ><HomeOutlined /></button>
            <Divider type='vertical' />
            <span className={style.designName}>{designName}</span>
            <button className={style.defBtn} style={{ width: 30, height: 30, fontSize: 13 }} ><CaretDownOutlined /></button>
            <div style={{ flex: 1 }}></div>
            <button className={style.functionBtn} style={{ marginRight: 30 }} onClick={() => handleSave(lastContent, uid)}>保存</button>
            <div>

            </div>
          </div>
          <div style={{ display: 'flex', minHeight: 0, flex: 1 }}>
            <div className={style.sider}>
              <div style={{ width: 60, borderRight: '1px solid #eaeaea' }}>
                <div style={{ display: 'flex', flexDirection: 'column', padding: 5 }}>
                  {btnItem.map((item) => (
                    <div style={{ width: '100%', marginBottom: 10 }} key={item.key}>
                      <button
                        className={currentBtnKey === item.key ? style.focusBtn : style.defBtn}
                        onClick={() => setCurrentBtnKey(item.key)}
                      >{item.icon}</button>
                      <div style={{ textAlign: 'center', fontSize: 13, marginTop: 5 }} className={currentBtnKey === item.key ? style.focusLabel : style.defLabel}>{item.label}</div>
                    </div>
                  ))}
                </div>
              </div>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                <div style={{ flex: 1, display: currentBtnKey === '1' ? 'block' : 'none', minHeight: 0 }}>
                  <div style={{ height: '100%', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                    <WordComponent modifyContent={selectText} tdId={design_id} />
                  </div>
                </div>
                <div style={{ flex: 1, display: currentBtnKey === '2' ? 'block' : 'none', minHeight: 0 }}>
                  <div style={{ height: '100%', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                    <ImgComponent modifyContent={selectImgText} tdId={design_id} />
                  </div>
                </div>
                <div style={{ flex: 1, display: currentBtnKey === '3' ? 'block' : 'none', minHeight: 0 }}>
                  <div style={{ height: '100%', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                    <VideoComponent modifyContent={selectVideoText} tdId={design_id} />
                  </div>
                </div>

              </div>
            </div>
            <div className={style.content}>
              <Toolbar
                editor={editor}
                defaultConfig={toolbarConfig}
                mode="default"

              />
              <Editor
                defaultConfig={editorConfig}
                value={designContent}
                onCreated={setEditor}
                onChange={(editor) => setLastContent(editor.getHtml())}
                mode="default"
                className={style.textEditor}
              />
              {/* <div>{html}</div> */}
            </div>
          </div>
        </div>
      </>}
    </ConfigProvider>
  )
}

export default TextEditor