import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { Editor} from '@wangeditor/editor-for-react'
import { DomEditor } from '@wangeditor/editor'
import testMarkdown from './testmm.md?raw'
import { useEffect, useState } from 'react'


function customCheckLinkFn(text, url) {
if (!url) {
    return
  }
  if (url.indexOf('http') !== 0) {
    return '链接必须以 http/https 开头'
  }
  return true
  // 返回值有三种选择：
  // 1. 返回 true ，说明检查通过，编辑器将正常插入链接
  // 2. 返回一个字符串，说明检查未通过，编辑器会阻止插入。会 alert 出错误信息（即返回的字符串）
  // 3. 返回 undefined（即没有任何返回），说明检查未通过，编辑器会阻止插入。但不会提示任何信息
}

 function customParseLinkUrl(url) {
 if (url.indexOf('http') !== 0) {
    return `http://${url}`
  }
  return url
}

const ResourceEditor = ({content}) =>{
 
 const [editor, setEditor] = useState(null) 
 // 转换Markdown
 const initialHtml = DOMPurify.sanitize(marked.parse(content))

 const editorConfig = {
    readOnly:true,
    autoLink: true,
    MENU_CONF: {
      link: {
        checkLink: customCheckLinkFn,
        parseLinkUrl: customParseLinkUrl,
      }
    }
   }
   
     useEffect(() => {
     if (editor) {
       // console.log('工具栏配置', editor.getSelectionPosition()
      
     }

     return () => {
       if (!editor) return
       editor.destroy()
       setEditor(null)
         }
     }, [editor])

return(
    <div>
        <Editor   
                  defaultConfig={editorConfig}
                  value={initialHtml}
                  onCreated={setEditor}
                  // onChange={(editor) => setHtml(editor.getHtml())}
                  mode="default"
                />
    </div>
)
}

export default ResourceEditor
