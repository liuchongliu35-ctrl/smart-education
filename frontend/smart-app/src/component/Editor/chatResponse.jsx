import { useState, useEffect, useRef, useCallback } from 'react'
import style from '@/pages/TextEditor/textEditor.module.css'
import { Divider, message } from 'antd'
import { CheckOutlined, CopyOutlined } from '@ant-design/icons'; 
import { MarkdownToHtml } from "./changeMarkdown"

  const ChatResponse = ({onChange,content}) =>{
    const markdownRef = useRef(null)


    const [displayContent,setDisplayContent] = useState('')
    
    useEffect(() => {
      setDisplayContent(content)
    }, [content])

    function useTypewriter(text,speed) {
        const [displayText, setDisplayText] = useState('')
      
        useEffect(() => {
          let index = 0;
          let currentText = ''
          
          const timer = setInterval(() => {
            if (index < text.length) {
              currentText += text.charAt(index)
              setDisplayText(currentText)
              scrollToBottom()
              index++
            } else {
              clearInterval(timer)
              onChange('2')
              setIsComplate(true)
            }
          }, 10)
      
          return () => clearInterval(timer)
        }, [text, speed]) // 当 text 或 speed 变化时重新运行
      
        return displayText
      }

    const scrollToBottom = useCallback(() => {
    if (markdownRef.current) {
      // 使用平滑滚动
      markdownRef.current.scrollTo({
        top: markdownRef.current.scrollHeight,
        behavior: 'smooth'
      })
    }
  }, [])
  

    const displayText = useTypewriter(displayContent)
    const [copied, setCopied] = useState(false)
    const [isComplate,setIsComplate] = useState (false)
   
    // 复制功能
  const handleCopy = async () => {
    try {
      const htmlContent = markdownRef.current.innerHTML
      const blob = new Blob([htmlContent], { type: 'text/html' })
      await navigator.clipboard.write([
        new ClipboardItem({
          'text/html': blob,
          // 'text/plain': new Blob([markdownRef.current.textContent], { type: 'text/plain' })
        })
      ])
      setCopied(true)
      message.success('已复制到剪贴板')
      
    } catch (err) {
      message.error('复制失败，请手动选择文本')
    }
  }

  

    return(
        <>
        <div 
        ref={markdownRef}
        className={style.generateTextContent}>
            <div 
            
            className={style.contentStyle}>
            <MarkdownToHtml content={displayText} nowRef={markdownRef} />
           {isComplate && 
           <>
           <Divider />
                 <button 
                 onClick={handleCopy}
                 className={style.copyBtn}
                 disabled={copied}>
                 {copied ? (
                   <>
                     <CheckOutlined /> 已复制
                   </>
                 ) : (
                   <>
                     <CopyOutlined /> 复制
                   </>
                 )}
               </button>
               </>
           }
          
            </div>
             
          </div>
        </>
    )
  }

  export default ChatResponse