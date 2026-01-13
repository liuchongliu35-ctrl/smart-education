import './style.css' // 引入 css
import React, { useState, useEffect } from 'react'
import { Editor, Toolbar } from '@wangeditor/editor-for-react'
import { Boot } from '@wangeditor/editor'
import style from '@/pages/TextEditor/textEditor.module.css'
import { useNavigate, useParams } from 'react-router-dom'
import Icon, {HomeOutlined,FormOutlined,CaretDownOutlined,CalendarOutlined} from '@ant-design/icons';
import { ConfigProvider,Layout,Divider, message} from 'antd'
import Example from '@/assets/png/合照.png'
import WordComponent from './wordComponent'
import VideoComponent from './videoComponent'
import ImgComponent from './imgComponent'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import 'katex/dist/katex.min.css'
import testMarkdown from './nice.md?raw'
import { getHistoryDesignContent,postEditorCache } from '../../apis/preparation'
import PageLoadingComponent from '../Loading/pageLoadingComponent'

const btnItem = [
  {
    key: '1',
    icon:<HomeOutlined />,
    label:'文案'
   },
  {
    key: '2',
    icon: <FormOutlined />,
    label:'图片'
  },{
    key: '3',
    icon:<CalendarOutlined />,
    label:'视频'
  }
]






const CustomTextEditor = () =>{
 

    const [editor, setEditor] = useState(null) 
    const [currentBtnKey,setCurrentBtnKey] = useState('1')    
    const [designContent,setDesignContent] = useState('第一章：空间几何体')
    const [designName,setDesignName] =useState('')
    const [selectText, setSelectText] = useState('')
    const [selectImgText,setSelectImgText] = useState('')
    const [selectVideoText,setSelectVideoText] = useState('')
    const [isLoading,setIsLoading] = useState(false)
    const [reload,setReload] = useState(false)
    const [messageApi, contextHolder] = message.useMessage()
    const [lastContent,setLastContent] = useState('')
    const [html,setHtml] = useState('')


    const navigate = useNavigate()
    let {design_id} = useParams()

    /* ——————数据请求———————— */
      const getDesignList = async() =>{
        setIsLoading(true)
        // let {data} = await getHistoryDesignContent(design_id)
        setDesignName(data.designName)}
    
      useEffect(()=>{
        // getDesignList()
      },[])
    
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
          menuKeys: ['through', 'sup', 'sub','code']
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

    const handleSave = async (content) => {
      const prop = {
        tdId:design_id,
        uid:12,
        newContents:content
      }
      console.log(prop)
      await postEditorCache(prop)
      setReload(pre => !pre)
      messageApi.open({
        type: 'success',
        content: '保存成功',
      })
    }


return(
  
    <ConfigProvider wave={{ disabled: true }}>
    {isLoading ? <PageLoadingComponent /> : <>
      {contextHolder}
    <div style={{display:'flex',flexDirection:'column',height:'100vh',border:'1px solid #eaeaea'}}>
    <div className={style.header}>
    <button className={style.defBtn} onClick={()=>navigate(-1)} style={{width:50,height:50}} ><HomeOutlined /></button>
    <Divider type='vertical' />
    <span className={style.designName}>{designName}</span>
    <button className={style.defBtn} style={{width:30,height:30,fontSize:13}} ><CaretDownOutlined /></button>
    <div style={{flex:1}}></div>
    <button className={style.functionBtn} style={{marginRight:30}} onClick={()=>handleSave(lastContent)}>保存</button>
    <div>

    </div>
    </div>
    <div style={{display:'flex',minHeight:0,flex:1}}>
    <div className={style.sider}>
      <div style={{width:60,borderRight:'1px solid #eaeaea'}}>
      <div style={{display:'flex',flexDirection:'column',padding:5}}>
        {btnItem.map((item)=>(
          <div style={{width:'100%',marginBottom:10}} key={item.key}>
          <button 
          className={currentBtnKey === item.key ? style.focusBtn : style.defBtn}
          onClick={()=>setCurrentBtnKey(item.key)}
          >{item.icon}</button>
          <div style={{textAlign:'center',fontSize:13,marginTop:5}} className={currentBtnKey === item.key ? style.focusLabel : style.defLabel}>{item.label}</div>
        </div>
        ))}
      </div>
      </div>
      <div style={{flex:1,display:'flex',flexDirection:'column'}}>
      <div style={{flex:1,display: currentBtnKey === '1' ? 'block' : 'none',minHeight:0}}>
        <div style={{height:'100%',display:'flex',flexDirection:'column',position:'relative'}}>
        <WordComponent modifyContent={selectText} tdId={design_id}  />
        </div>
      </div>
      <div style={{flex:1,display: currentBtnKey === '2' ? 'block' : 'none',minHeight:0}}>
      <div style={{height:'100%',display:'flex',flexDirection:'column',position:'relative'}}>
      <ImgComponent modifyContent={selectImgText} tdId={design_id}  />
        </div>
      </div>
      <div style={{flex:1,display: currentBtnKey === '3' ? 'block' : 'none',minHeight:0}}>
      <div style={{height:'100%',display:'flex',flexDirection:'column',position:'relative'}}>
      <VideoComponent modifyContent={selectVideoText} tdId={design_id}  />
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
          value={html}
          onCreated={setEditor}
          onChange={(editor) => setHtml(editor.getHtml())}
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

export default CustomTextEditor