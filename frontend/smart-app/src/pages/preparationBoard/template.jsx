import Icon, { AntDesignOutlined, CodeSandboxOutlined, OpenAIOutlined } from '@ant-design/icons'
import { Button, Card, ConfigProvider, Divider, Modal } from 'antd'
import { useNavigate } from 'react-router-dom'
import SelectTemplate from './selectTemplate'
import { useEffect, useState } from 'react'
import style from './preparationBoard.module.css'
import { getDesignTemplateList } from '../../apis/preparation'

const HeartSvg = () => (
  <svg t="1740647456914" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5372" width="64" height="64"><path d="M548.256 175.808l-56.576 56.56 259.728 259.728L491.68 751.808l56.56 56.56 316.304-316.272-316.288-316.288z m-304.016 0l-56.56 56.56 259.728 259.728L187.68 751.808l56.56 56.56 316.288-316.272-316.288-316.288z" fill="#565D64" p-id="5373"></path></svg>
)



const QuitIcon = (props) => <Icon component={HeartSvg} {...props} />
// const CardIcon = (props) => <Icon component={CardSvg} {...props} />

const colorList = [
  { backgroundStyle: '#B32E59', top: '#F99113', middle: '#FD6452', bottom: '#EC525C' },
  { backgroundStyle: '#1C2278', top: '#4ACBEF', middle: '#0BA2F2', bottom: '#014BF8' },
  { backgroundStyle: '#8EBDCD', top: '#A3D7F5', middle: '#C9E4F9', bottom: '#D4D7FB' },
  { backgroundStyle: '#FAA6C0', top: '#d369fa', middle: '#E140FE', bottom: '#C040FB' },
  { backgroundStyle: '#5802C3', top: '#DE2EF2', middle: '#9A1BFC', bottom: '#7126F5' },
  { backgroundStyle: '#F9D71D', top: '#FFEB01', middle: '#F77341', bottom: '#F6669A' },
]



const quitIconStyle = {
  width: '3vw',
  transform: 'scaleX(-1)',
  cursor: 'pointer',
  position: 'absolute',
  top: '2vh',
  left: '3%'
}

const defaultCardList = [
  { text: '默认模板一' },
  { text: '默认模板二' },
  { text: '默认模板三' },
]

const BoardTemplate = () => {
  const navigate = useNavigate()
  const uid = sessionStorage.getItem('uid')

  const [open, setOpen] = useState(false);
  const [confirmLoading, setConfirmLoading] = useState(false)
  const [templateList, setTemplateList] = useState([])
  const [selectedData, setSelectedData] = useState({})

  /* 弹窗控制区域 */
  const showModal = (item) => {
    setSelectedData(item)
    setTimeout(() => {
      setOpen(true);
    }, 50)
  }
  const handleOk = () => {
    setConfirmLoading(true)
    setTimeout(() => {
      setOpen(false)
      setConfirmLoading(false)
    }, 1000)
  }
  const handleCancel = () => {
    setOpen(false)
  }
  /* ——————数据请求———————— */
  const getSyllabusList = async () => {
    let { data } = await getDesignTemplateList(uid)
    setTemplateList(data)
    console.log(data)
  }

  useEffect(() => {
    getSyllabusList()
  }, [])

  return (
    <ConfigProvider>
      <QuitIcon style={quitIconStyle} onClick={() => navigate(-1)} />
      <button className={style.functionBtn} style={{ position: 'absolute', right: 100, top: 20, fontWeight: 600, fontSize: 16, paddingInline: 20 }}>编辑</button>
      <div style={{ marginTop: 50, marginBottom: 50 }}>
        <div style={{ width: '80%', padding: 30, margin: 'auto' }}>
          <div style={{ color: '#000', lineHeight: 2, fontSize: 20, textAlign: 'left', marginBottom: 20, fontFamily: 'siyuan' }}>已保存的大纲</div>
          <div style={{ display: 'flex', width: '100%', flexWrap: 'wrap' }}>
            {templateList?.map((item, index) => (
              <div
                className={style.templateCardItem}
                style={{ width: 200, lineHeight: 2, marginRight: 60, cursor: 'pointer', marginBottom: 30, position: 'relative' }}
                onClick={() => showModal(item)}
                key={index}>
                <div className={style.templates} style={{ background: colorList[item.num].backgroundStyle }}>
                  <div style={{ fontSize: 16, color: '#fff', textAlign: 'left', fontFamily: 'siyuan' }}>{item.name}</div>
                  <svg width={60} viewBox="0 0 1024 1024" style={{ marginTop: 15 }}>
                    <path d="M913.92 275.968l-375.296-206.848c-15.36-8.192-33.792-8.704-49.152 0l-379.392 206.848c-18.432 10.24-29.696 30.208-29.696 51.2-0.512 20.992 10.24 40.448 28.16 51.712l375.296 221.696c15.872 9.728 35.84 9.728 52.224 0l379.392-221.696c17.92-10.752 28.672-30.72 28.16-51.712-0.512-21.504-11.776-41.472-29.696-51.2z m-403.968 269.824l-368.64-217.6 372.736-203.264 368.64 202.752-372.736 218.112z" p-id="2840" data-spm-anchor-id="a313x.search_index.0.i1.723c3a81ZVRISK" class="" fill={colorList[item.num].top}></path><path d="M902.656 510.464l-77.312-43.52-55.808 36.864 62.976 35.328-322.56 194.56-318.464-194.048 67.072-37.376-55.808-37.376-81.408 45.056c-9.728 5.632-15.872 16.384-15.872 27.648-0.512 11.264 5.12 22.016 14.848 28.672l375.296 225.28c4.096 2.56 9.216 4.096 14.336 4.096 5.12 0 9.728-1.536 13.824-4.096l379.392-225.28c9.728-6.144 15.36-16.896 14.848-28.16 0.512-11.264-5.632-22.016-15.36-27.648z" p-id="2841" data-spm-anchor-id="a313x.search_index.0.i3.723c3a81ZVRISK" class="" fill={colorList[item.num].middle}></path><path d="M902.656 670.72l-75.264-41.472-12.288 38.912 19.456 30.72-324.608 199.168-320.512-198.656 22.528-30.208-11.776-39.424-78.848 40.448c-9.728 6.144-15.872 16.896-15.872 28.16 0 11.776 5.632 23.04 14.848 28.672l375.296 229.376c4.608 2.56 9.216 4.096 14.336 4.096 5.12 0 9.728-1.536 13.824-4.096l379.392-229.376c9.728-6.144 15.36-17.408 14.848-28.672 0.512-10.752-5.12-21.504-15.36-27.648z" p-id="2842" data-spm-anchor-id="a313x.search_index.0.i4.723c3a81ZVRISK" class="selected" fill={colorList[item.num].bottom}></path>
                  </svg>
                  <div style={{ fontSize: 11, marginTop: 10, color: "#fff", position: 'absolute', bottom: 10, right: 8 }}>{item.createTime}</div>
                </div>
                <div style={{ textAlign: 'left' }}>{item.text}</div>
              </div>
            ))}
          </div>
          <Divider />
          <div style={{ color: '#000', lineHeight: 2, fontSize: 20, textAlign: 'left', marginTop: 20, marginBottom: 20, fontFamily: 'siyuan' }}>默认模板</div>
          <div style={{ display: 'flex', width: '100%', flexWrap: 'wrap' }}>
            {defaultCardList.map((item, index) => (<>
              <div
                className={style.templateCardItem}
                style={{ color: '#000', width: 200, lineHeight: 2, marginRight: 60, cursor: 'pointer' }} key={index}>
                <div className={style.templates} style={{ background: colorList[index + 3].backgroundStyle }}>
                  <div style={{ fontSize: 16, color: '#fff', textAlign: 'left', fontFamily: 'siyuan' }}>模板一</div>
                  <svg width={60} viewBox="0 0 1024 1024" style={{ marginTop: 15 }}>
                    <path d="M913.92 275.968l-375.296-206.848c-15.36-8.192-33.792-8.704-49.152 0l-379.392 206.848c-18.432 10.24-29.696 30.208-29.696 51.2-0.512 20.992 10.24 40.448 28.16 51.712l375.296 221.696c15.872 9.728 35.84 9.728 52.224 0l379.392-221.696c17.92-10.752 28.672-30.72 28.16-51.712-0.512-21.504-11.776-41.472-29.696-51.2z m-403.968 269.824l-368.64-217.6 372.736-203.264 368.64 202.752-372.736 218.112z" p-id="2840" data-spm-anchor-id="a313x.search_index.0.i1.723c3a81ZVRISK" class="" fill={colorList[index + 3].top}></path><path d="M902.656 510.464l-77.312-43.52-55.808 36.864 62.976 35.328-322.56 194.56-318.464-194.048 67.072-37.376-55.808-37.376-81.408 45.056c-9.728 5.632-15.872 16.384-15.872 27.648-0.512 11.264 5.12 22.016 14.848 28.672l375.296 225.28c4.096 2.56 9.216 4.096 14.336 4.096 5.12 0 9.728-1.536 13.824-4.096l379.392-225.28c9.728-6.144 15.36-16.896 14.848-28.16 0.512-11.264-5.632-22.016-15.36-27.648z" p-id="2841" data-spm-anchor-id="a313x.search_index.0.i3.723c3a81ZVRISK" class="" fill={colorList[index + 3].middle}></path><path d="M902.656 670.72l-75.264-41.472-12.288 38.912 19.456 30.72-324.608 199.168-320.512-198.656 22.528-30.208-11.776-39.424-78.848 40.448c-9.728 6.144-15.872 16.896-15.872 28.16 0 11.776 5.632 23.04 14.848 28.672l375.296 229.376c4.608 2.56 9.216 4.096 14.336 4.096 5.12 0 9.728-1.536 13.824-4.096l379.392-229.376c9.728-6.144 15.36-17.408 14.848-28.672 0.512-10.752-5.12-21.504-15.36-27.648z" p-id="2842" data-spm-anchor-id="a313x.search_index.0.i4.723c3a81ZVRISK" class="selected" fill={colorList[index + 3].bottom}></path>
                  </svg>
                </div>
              </div>
            </>
            ))}
          </div>
        </div>
      </div>

      <Modal
        width={'max-content'}
        confirmLoading={confirmLoading}
        open={open}
        maskClosable={true}
        cancelButtonProps={{ style: { display: 'none' } }}
        okButtonProps={{ style: { display: 'none' } }}
        onCancel={handleCancel}
        destroyOnClose
      >
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          {<SelectTemplate
            templateData={selectedData}
            onSubmit={handleOk}
          />}
        </div>
      </Modal>

    </ConfigProvider>
  )
}

export default BoardTemplate