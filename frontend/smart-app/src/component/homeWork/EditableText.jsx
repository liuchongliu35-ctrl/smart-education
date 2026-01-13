import { Input } from "antd"
import style from '@/pages/homeWork/homeWork.module.css'
import { useEffect, useState } from "react"
import MathProblemRenderer from "../Editor/renderMath"


const { TextArea } = Input

const EditableText = ({ key, value, onChange }) => {
  const [editable, setEditable] = useState(false)
  const [text, setText] = useState('')

  useEffect(() => {
    setText(value)
  }, [value, key])

  const handleSave = () => {
    onChange(text)  // 触发父组件更新
    setEditable(false)
  }
  return editable ? (
    <TextArea
      style={{ fontSize: 18, width: 800 }}
      value={text}
      onChange={(e) => setText(e.target.value)}
      onBlur={handleSave}
      onPressEnter={handleSave}
      autoFocus
    />
  ) : (
    <span onClick={() => setEditable(true)}>{text}</span>
    // <MathProblemRenderer content={text} onClick={() => setEditable(true)} />
  )
}

export default EditableText
