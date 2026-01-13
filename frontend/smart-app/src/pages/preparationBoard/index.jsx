import { useNavigate, useParams } from 'react-router-dom'
import style from './preparationBoard.module.css'
import Icon, { RestOutlined, SendOutlined, ArrowRightOutlined } from '@ant-design/icons'
import { Avatar, Button, ConfigProvider, message, Modal, Popconfirm, Popover, Switch, Tag } from 'antd'
import PreparationBoardHistory from './history'
import { useEffect, useState } from 'react'
import AIpreparation from './AIpreparation'
import designData from '@/assets/JSON/10条教学设计.json'
import Book from '@/assets/png/13.png'
import { getAllDesignList, deleteDesignAPI } from '../../apis/preparation'
import NoneDataIcon from '@/assets/svg/暂无内容.svg'


const HeartSvg = () => (
  <svg t="1740647456914" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5372" width="64" height="64"><path d="M548.256 175.808l-56.576 56.56 259.728 259.728L491.68 751.808l56.56 56.56 316.304-316.272-316.288-316.288z m-304.016 0l-56.56 56.56 259.728 259.728L187.68 751.808l56.56 56.56 316.288-316.272-316.288-316.288z" fill="#565D64" p-id="5373"></path></svg>
)
const TemplateSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>heart icon</title>
    <path d="M921.6 102.4v358.4h-256V102.4h256M358.4 102.4v153.6H102.4V102.4h256m0 460.8v358.4H102.4v-358.4h256m563.2 204.8v153.6h-256v-153.6h256m0-768h-256c-56.32 0-102.4 46.08-102.4 102.4v358.4c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4V102.4c0-56.32-46.08-102.4-102.4-102.4zM358.4 0H102.4C46.08 0 0 46.08 0 102.4v153.6c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4V102.4c0-56.32-46.08-102.4-102.4-102.4z m0 460.8H102.4c-56.32 0-102.4 46.08-102.4 102.4v358.4c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4v-358.4c0-56.32-46.08-102.4-102.4-102.4z m563.2 204.8h-256c-56.32 0-102.4 46.08-102.4 102.4v153.6c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4v-153.6c0-56.32-46.08-102.4-102.4-102.4z" fill="#fff" p-id="7171"></path>
  </svg>
)
const TemplateBgSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>heart icon</title>
    <path d="M921.6 102.4v358.4h-256V102.4h256M358.4 102.4v153.6H102.4V102.4h256m0 460.8v358.4H102.4v-358.4h256m563.2 204.8v153.6h-256v-153.6h256m0-768h-256c-56.32 0-102.4 46.08-102.4 102.4v358.4c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4V102.4c0-56.32-46.08-102.4-102.4-102.4zM358.4 0H102.4C46.08 0 0 46.08 0 102.4v153.6c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4V102.4c0-56.32-46.08-102.4-102.4-102.4z m0 460.8H102.4c-56.32 0-102.4 46.08-102.4 102.4v358.4c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4v-358.4c0-56.32-46.08-102.4-102.4-102.4z m563.2 204.8h-256c-56.32 0-102.4 46.08-102.4 102.4v153.6c0 56.32 46.08 102.4 102.4 102.4h256c56.32 0 102.4-46.08 102.4-102.4v-153.6c0-56.32-46.08-102.4-102.4-102.4z" fill="#6464647b" p-id="7171"></path>
  </svg>
)
const AISvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>heart icon</title>
    <path d="M409.6 750.933l34.133 68.267H170.667v136.533h682.666V819.2H580.267l34.133-68.267h238.933A68.267 68.267 0 0 1 921.6 819.2v136.533A68.267 68.267 0 0 1 853.333 1024H170.667a68.267 68.267 0 0 1-68.267-68.267V819.2a68.267 68.267 0 0 1 68.267-68.267H409.6zM273.067 68.267h477.866a68.267 68.267 0 0 1 68.267 68.266V614.4a68.267 68.267 0 0 1-68.267 68.267H273.067A68.267 68.267 0 0 1 204.8 614.4V136.533a68.267 68.267 0 0 1 68.267-68.266z m0 68.266V614.4h477.866V136.533H273.067z m614.4 102.4a34.133 34.133 0 0 1 34.133 34.134v204.8a34.133 34.133 0 1 1-68.267 0v-204.8a34.133 34.133 0 0 1 34.134-34.134z m-750.934 0a34.133 34.133 0 0 1 34.134 34.134v204.8a34.133 34.133 0 0 1-68.267 0v-204.8a34.133 34.133 0 0 1 34.133-34.134zM989.867 307.2A34.133 34.133 0 0 1 1024 341.333V409.6a34.133 34.133 0 1 1-68.267 0v-68.267a34.133 34.133 0 0 1 34.134-34.133z m-955.734 0a34.133 34.133 0 0 1 34.134 34.133V409.6A34.133 34.133 0 0 1 0 409.6v-68.267A34.133 34.133 0 0 1 34.133 307.2z m341.334 102.4a51.2 51.2 0 1 0 0-102.4 51.2 51.2 0 0 0 0 102.4z m273.066 0a51.2 51.2 0 1 0 0-102.4 51.2 51.2 0 0 0 0 102.4zM512 0a34.133 34.133 0 0 1 34.133 34.133V102.4a34.133 34.133 0 0 1-68.266 0V34.133A34.133 34.133 0 0 1 512 0z m-68.267 614.4a34.133 34.133 0 0 1 34.134 34.133v136.534a34.133 34.133 0 1 1-68.267 0V648.533a34.133 34.133 0 0 1 34.133-34.133z m136.534 0a34.133 34.133 0 0 1 34.133 34.133v136.534a34.133 34.133 0 1 1-68.267 0V648.533a34.133 34.133 0 0 1 34.134-34.133z" p-id="2942" fill="#ffffff"></path>
  </svg>
)
const AIBgSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>heart icon</title>
    <path d="M409.6 750.933l34.133 68.267H170.667v136.533h682.666V819.2H580.267l34.133-68.267h238.933A68.267 68.267 0 0 1 921.6 819.2v136.533A68.267 68.267 0 0 1 853.333 1024H170.667a68.267 68.267 0 0 1-68.267-68.267V819.2a68.267 68.267 0 0 1 68.267-68.267H409.6zM273.067 68.267h477.866a68.267 68.267 0 0 1 68.267 68.266V614.4a68.267 68.267 0 0 1-68.267 68.267H273.067A68.267 68.267 0 0 1 204.8 614.4V136.533a68.267 68.267 0 0 1 68.267-68.266z m0 68.266V614.4h477.866V136.533H273.067z m614.4 102.4a34.133 34.133 0 0 1 34.133 34.134v204.8a34.133 34.133 0 1 1-68.267 0v-204.8a34.133 34.133 0 0 1 34.134-34.134z m-750.934 0a34.133 34.133 0 0 1 34.134 34.134v204.8a34.133 34.133 0 0 1-68.267 0v-204.8a34.133 34.133 0 0 1 34.133-34.134zM989.867 307.2A34.133 34.133 0 0 1 1024 341.333V409.6a34.133 34.133 0 1 1-68.267 0v-68.267a34.133 34.133 0 0 1 34.134-34.133z m-955.734 0a34.133 34.133 0 0 1 34.134 34.133V409.6A34.133 34.133 0 0 1 0 409.6v-68.267A34.133 34.133 0 0 1 34.133 307.2z m341.334 102.4a51.2 51.2 0 1 0 0-102.4 51.2 51.2 0 0 0 0 102.4z m273.066 0a51.2 51.2 0 1 0 0-102.4 51.2 51.2 0 0 0 0 102.4zM512 0a34.133 34.133 0 0 1 34.133 34.133V102.4a34.133 34.133 0 0 1-68.266 0V34.133A34.133 34.133 0 0 1 512 0z m-68.267 614.4a34.133 34.133 0 0 1 34.134 34.133v136.534a34.133 34.133 0 1 1-68.267 0V648.533a34.133 34.133 0 0 1 34.133-34.133z m136.534 0a34.133 34.133 0 0 1 34.133 34.133v136.534a34.133 34.133 0 1 1-68.267 0V648.533a34.133 34.133 0 0 1 34.134-34.133z" p-id="2942" fill="#6464647b"></path>
  </svg>
)
const CustomSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>heart icon</title>
    <path d="M727.008 487.232l194.016-184.32a99.2 99.2 0 0 0 0-140.288l-48.416-48.416a99.2 99.2 0 0 0-138.464-1.76L544.64 292.384 360.576 95.744l-1.504-1.568a64.832 64.832 0 0 0-91.712-0.384L129.184 231.968a64.8 64.8 0 0 0-1.12 90.144L309.408 515.84 137.952 678.72A99.264 99.264 0 0 0 109.696 728L80.704 851.744a65.632 65.632 0 0 0 82.4 77.92L282.4 894.528a99.744 99.744 0 0 0 40.32-23.232l169.056-160.608 203.616 217.536 1.504 1.568a64.832 64.832 0 0 0 91.712 0.384L926.784 792a64.8 64.8 0 0 0 1.12-90.144L727.008 487.232zM319.424 786.176l-90.112-90.112a31.488 31.488 0 0 0-9.792-6.496l447.584-425.216 94.272 94.272c1.408 1.408 3.168 2.08 4.768 3.168l-446.72 424.384z m458.784-627.392a35.2 35.2 0 0 1 49.12 0.64l48.416 48.416c13.76 13.76 13.76 36.032-0.64 50.4l-64.448 61.216c-1.28-2.08-2.24-4.288-4.064-6.112l-93.12-93.12 64.736-61.44z m-489.696 241.12c8-0.128 16-3.168 22.112-9.28l48-48a31.968 31.968 0 1 0-45.248-45.248l-48 48a31.68 31.68 0 0 0-8.928 20.256L174.816 278.4c-0.512-0.512-0.512-1.024-0.352-1.152L312.64 139.04c0.128-0.128 0.672-0.128 1.248 0.416l184.384 196.992L355.84 471.776l-67.328-71.872zM145.024 868.288a1.6 1.6 0 0 1-2.016-1.92L172 742.624c0.992-4.16 2.944-7.968 5.312-11.488a31.808 31.808 0 0 0 6.752 10.144l88.288 88.288a35.072 35.072 0 0 1-8 3.552l-119.328 35.168zM743.36 884.96c-0.128 0.128-0.672 0.128-1.248-0.416l-125.6-134.176a31.232 31.232 0 0 0 14.08-7.712l48-48a31.968 31.968 0 1 0-45.248-45.248l-48 48a31.68 31.68 0 0 0-7.296 11.904l-39.904-42.656 142.432-135.328 200.576 214.304c0.48 0.512 0.48 1.024 0.352 1.152L743.36 884.96z" p-id="4025" fill="#ffffff"></path>
  </svg>
)
const CustomBgSvg = ({ width }) => (
  <svg width={width} viewBox="0 0 1024 1024">
    <title>heart icon</title>
    <path d="M727.008 487.232l194.016-184.32a99.2 99.2 0 0 0 0-140.288l-48.416-48.416a99.2 99.2 0 0 0-138.464-1.76L544.64 292.384 360.576 95.744l-1.504-1.568a64.832 64.832 0 0 0-91.712-0.384L129.184 231.968a64.8 64.8 0 0 0-1.12 90.144L309.408 515.84 137.952 678.72A99.264 99.264 0 0 0 109.696 728L80.704 851.744a65.632 65.632 0 0 0 82.4 77.92L282.4 894.528a99.744 99.744 0 0 0 40.32-23.232l169.056-160.608 203.616 217.536 1.504 1.568a64.832 64.832 0 0 0 91.712 0.384L926.784 792a64.8 64.8 0 0 0 1.12-90.144L727.008 487.232zM319.424 786.176l-90.112-90.112a31.488 31.488 0 0 0-9.792-6.496l447.584-425.216 94.272 94.272c1.408 1.408 3.168 2.08 4.768 3.168l-446.72 424.384z m458.784-627.392a35.2 35.2 0 0 1 49.12 0.64l48.416 48.416c13.76 13.76 13.76 36.032-0.64 50.4l-64.448 61.216c-1.28-2.08-2.24-4.288-4.064-6.112l-93.12-93.12 64.736-61.44z m-489.696 241.12c8-0.128 16-3.168 22.112-9.28l48-48a31.968 31.968 0 1 0-45.248-45.248l-48 48a31.68 31.68 0 0 0-8.928 20.256L174.816 278.4c-0.512-0.512-0.512-1.024-0.352-1.152L312.64 139.04c0.128-0.128 0.672-0.128 1.248 0.416l184.384 196.992L355.84 471.776l-67.328-71.872zM145.024 868.288a1.6 1.6 0 0 1-2.016-1.92L172 742.624c0.992-4.16 2.944-7.968 5.312-11.488a31.808 31.808 0 0 0 6.752 10.144l88.288 88.288a35.072 35.072 0 0 1-8 3.552l-119.328 35.168zM743.36 884.96c-0.128 0.128-0.672 0.128-1.248-0.416l-125.6-134.176a31.232 31.232 0 0 0 14.08-7.712l48-48a31.968 31.968 0 1 0-45.248-45.248l-48 48a31.68 31.68 0 0 0-7.296 11.904l-39.904-42.656 142.432-135.328 200.576 214.304c0.48 0.512 0.48 1.024 0.352 1.152L743.36 884.96z" p-id="4025" fill="#6464647b"></path>
  </svg>
)

const TemplateIcon = (props) => <Icon component={TemplateSvg} {...props} />
const QuitIcon = (props) => <Icon component={HeartSvg} {...props} />
const AIIcon = (props) => <Icon component={AISvg} {...props} />
const CustomIcon = (props) => <Icon component={CustomSvg} {...props} />
const TemplateBgIcon = (props) => <Icon component={TemplateBgSvg} {...props} />
const AIBgIcon = (props) => <Icon component={AIBgSvg} {...props} />
const CustomBgIcon = (props) => <Icon component={CustomBgSvg} {...props} />





const PreparationBoardPage = () => {
  let { tsId, schoolId } = useParams()
  const navigate = useNavigate()
  const uid = sessionStorage.getItem('uid')
  const [open, setOpen] = useState(false);
  const [confirmLoading, setConfirmLoading] = useState(false)
  const [historyList, setHistoryList] = useState([])
  const [reload, setReload] = useState(false)
  const [messageApi, contextHolder] = message.useMessage()



  /* 弹窗控制区域 */
  const showModal = (e) => {
    setTimeout(() => {
      setOpen(true)
    }, 50);
  }
  const handleOk = () => {
    setConfirmLoading(true);
    setTimeout(() => {
      setOpen(false);
      setConfirmLoading(false);
    }, 2000)
  }
  const handleCancel = () => {
    setOpen(false)
  }

  /* ——————数据请求———————— */
  const getDesignList = async () => {
    let { data } = await getAllDesignList(uid)
    setHistoryList(data.slice(0, 9))
    console.log(data)
  }

  useEffect(() => {
    getDesignList()
  }, [reload])

  const handleDelete = async (tdId) => {
    await deleteDesignAPI(tdId)
    setReload(pre => !pre)
    messageApi.open({
      type: 'success',
      content: '删除成功',
    })
  }



  return (
    <ConfigProvider
      theme={{
        components: {
          Modal: {
            contentBg: '#Fff'
          },
          Button: {
            defaultBg: '#283f7e7a',
          }
        },
      }}>
      {contextHolder}
      <div style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
        {/* <div className={style.titleText}>选择备课板类型</div> */}
        <div className={style.box}>
          <div style={{ width: '100%', textAlign: 'left', fontSize: 22, fontFamily: 'siyuan' }}>选择模板类型</div>
          <div style={{ width: '90%', margin: 'auto', padding: 20, height: '100%', display: 'flex', justifyContent: 'space-between' }}>
            <div className={style.templateCard} onClick={() => navigate(`/home/template/${tsId}/${schoolId}`)}>
              <div style={{ textAlign: 'left', fontSize: 18, fontFamily: 'siyuan', display: 'flex', alignItems: 'center' }}> <TemplateIcon width='30px' style={{ marginRight: 5 }} />模板库</div>
              <div style={{ marginTop: 15, fontSize: 13 }}>「适合追求效率的常规教学」</div>
              <div className={style.entryCircle}><ArrowRightOutlined /></div>
              <div className={style.IconBgStyle}><TemplateBgIcon /></div>
            </div>
            <div className={style.templateCard} onClick={() => showModal()}>
              <div style={{ textAlign: 'left', fontSize: 18, fontFamily: 'siyuan', display: 'flex', alignItems: 'center' }}> <AIIcon width='30px' style={{ marginRight: 5 }} />AI生成大纲</div>
              <div style={{ marginTop: 15, fontSize: 13 }}>「让AI成为您的备课合伙人」</div>
              <div className={style.entryCircle}><ArrowRightOutlined /></div>
              <div className={style.IconBgStyle}><AIBgIcon /></div>
            </div>
            <div className={style.templateCard} onClick={() => navigate('/customeditor')}>
              <div style={{ textAlign: 'left', fontSize: 18, fontFamily: 'siyuan', display: 'flex', alignItems: 'center' }}> <CustomIcon width='30px' style={{ marginRight: 5 }} />自定义大纲</div>
              <div style={{ marginTop: 15, fontSize: 13 }}>「您的教学风格，由您定义」</div>
              <div className={style.entryCircle}><ArrowRightOutlined /></div>
              <div className={style.IconBgStyle}><CustomBgIcon /></div>
            </div>
          </div>
        </div>

        {/* 历史纪录列表 */}
        <div className={style.historyBox}>
          <div style={{ width: '100%', textAlign: 'left', fontSize: 22, fontFamily: 'siyuan' }}>历史记录</div>
          <div style={{ width: '100%', padding: 20 }}>
            <div className={style.historyListBox}>
              {historyList && historyList.length > 0 ? (
                <>
                  {historyList?.map((item, index) => (
                    <div className={style.historyListItem} key={index} >
                      <div style={{ fontFamily: 'siyuan', fontSize: 16, width: 260, textAlign: 'left' }}>
                        <Avatar src={Book} size={50} style={{ marginRight: 10 }} />
                        {item.designName}</div>
                      {/* <div>1234字</div> */}
                      <div style={{ width: 200, textAlign: 'left', fontSize: 13 }}>创建时间: {item.createTime} </div>
                      <div style={{ width: 200, textAlign: 'left', fontSize: 13 }}>最近修改：{item.lastModify}</div>
                      <div style={{ width: 100, textAlign: 'left', fontSize: 13 }}>授课时间: {item.classTime} </div>
                      {/* <div><Tag  color="magenta">数学</Tag> </div> */}
                      <div style={{ width: 100, display: 'flex' }}>
                        <Popover content="点击编辑" >
                          <button className={style.functionBtn} style={{ marginRight: 20 }} onClick={() => navigate(`/texteditor/${item.tdId}`)}><SendOutlined style={{ fontSize: 16 }} /></button>
                        </Popover>

                        <Popover content="点击删除" >
                          <Popconfirm
                            title="删除"
                            description="你确定要删除这条教学设计吗?"
                            onConfirm={() => handleDelete(item.tdId)}
                            okText="是"
                            cancelText="否"
                          >
                            <button className={style.deleteBtn}><RestOutlined style={{ fontSize: 16 }} /></button>

                          </Popconfirm>

                        </Popover>

                      </div>
                    </div>
                  ))}
                  <div style={{ fontSize: 18, height: 50, lineHeight: '50px', cursor: 'pointer' }} onClick={() => navigate('/home/preparationhistory')}>查看更多 · · ·</div>
                </>)
                : (
                  <div style={{ paddingBottom: 30 }}>
                    <Avatar src={NoneDataIcon} size={128} />
                    <div style={{ fontFamily: 'youshe', color: '#000', fontSize: 22 }}>暂无数据</div>
                  </div>
                )}

            </div>
          </div>
        </div>
      </div>

      <Modal
        width={'max-content'}
        open={open}
        confirmLoading={confirmLoading}
        maskClosable={true}
        cancelButtonProps={{ style: { display: 'none' } }}
        okButtonProps={{ style: { display: 'none' } }}
        onCancel={handleCancel}
        destroyOnClose
      >
        <AIpreparation />
      </Modal>

    </ConfigProvider>
  )
}
export default PreparationBoardPage