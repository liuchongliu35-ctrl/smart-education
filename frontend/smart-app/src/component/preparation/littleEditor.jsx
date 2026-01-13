import { Editor, Toolbar } from '@wangeditor/editor-for-react'
import { useEffect, useState } from 'react'
import ReactMarkdown from 'react-markdown'
import { renderToString } from 'react-dom/server'
import { createElement } from 'react'
import { marked } from 'marked'
import DOMPurify from 'dompurify'


const LittleEditor = ({returnHtml}) =>{
        const [editor, setEditor] = useState(null) 
        const [generate,setGenerate] = useState('')

        useEffect(()=>{
          if(returnHtml){
            setGenerate(returnHtml)
            console.log(returnHtml)
          }
        },[returnHtml])

        // 转换Markdown
        const dirtyHtml = marked.parse(generate)
        const cleanHtml = DOMPurify.sanitize(dirtyHtml)
        const content = cleanHtml

          // 编辑器配置
       const editorConfig = {
        
        placeholder: '暂无内容',
        readOnly : true
       
       }

 // 及时销毁 editor ，重要！
    useEffect(() => {
      return () => {
        if (editor == null) return
        editor.destroy()
        setEditor(null)
      }
    }, [editor])

    return (
        <>
        <Editor   
                  defaultConfig={editorConfig}
                  value={content}
                  onCreated={setEditor}
                  // onChange={(editor) => setContent(editor.getHtml())}
                  mode="default"
                  
                />
        
        </>
    )
}


export default LittleEditor