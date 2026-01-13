import { useLocation, useNavigate } from 'react-router-dom'
import style from './home.module.css'
import Icon, { } from '@ant-design/icons'
import { Avatar, Divider, message, Progress } from 'antd'
import ProgressIcon from '../../assets/svg/人机交互.svg'
import HomeworkIcon from '../../assets/svg/习题图标.svg'
import ClassIcon from '../../assets/svg/合作客户.svg'
import PreparationIcon from '../../assets/svg/备课板图标.svg'
import KnowledgeGra from '../../assets/svg/知识图谱.svg'
import KnowledgeCardIcon from '../../assets/svg/知识库.svg'
import VideoIcon from '../../assets/svg/视频制作.svg'
import PPTIcon from '../../assets/svg/PPT制作.svg'
import { getLastPreparation } from '../../apis/preparation'
import { useEffect, useState } from 'react'
import { getUserInfo } from '../../apis/user'
import NoneData from '../../assets/svg/暂无内容.svg'

const HomeContent = () => {
    const location = useLocation()
    const navigate = useNavigate()
    const [lastDesignName, setLastDesignName] = useState('')
    const [lastModify, setLastModify] = useState('')
    const [dId, setDId] = useState()
    const [messageApi, contextHolder] = message.useMessage()
    const uid = sessionStorage.getItem('uid')
    const [tsId, setTsId] = useState('')
    const [schooldId, setSchoolId] = useState('')

    //数据请求
    const getData = async () => {
        let { data } = await getLastPreparation(uid)
        setLastDesignName(data[0].designName)
        setLastModify(data[0].lastModify)
        setDId(data[0].tdId)
    }

    //获取教师ID及学校
    const getInfoData = async () => {
        const { data } = await getUserInfo()
        setTsId(data.tsId)
        setSchoolId(data.schoolId)
    }

    useEffect(() => {
        if (location.state?.showLoginSuccess) {
            messageApi.open({
                type: 'success',
                content: '登录成功！欢迎回来！',
            })

            // 清除状态，避免刷新后重复显示
            window.history.replaceState({}, document.title);
        }
        getInfoData()
        getData()
    }, [location.state])

    function handleNavigate(e) {
        navigate(e)
    }

    return (
        <div style={{ width: '100%', padding: 20, height: '100%' }}>
            {contextHolder}
            <div className={style.generation} style={{ flexDirection: 'column' }}>
                <div className={style.titleTextBox}>
                    <div style={{ width: 'max-content', height: 'max-content' }}>Advancing creativity</div>
                    <div style={{ width: 'max-content', height: 'max-content' }}>with artificial intelligence</div>
                </div>
                <div style={{ lineHeight: 1.5, marginBottom: 20, fontFamily: 'siyuan', fontSize: 25, color: '#000', width: '84%', textAlign: 'left' }}>快速开始</div>
                <div className={style.entryBox}>

                    <div className={style.entryStyle}>
                        <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
                            <span style={{ fontSize: 24, fontFamily: 'none', fontWeight: 600 }}>备课板</span>
                            <Divider type='vertical' />
                            <span style={{ color: '#727272' }}>preparation board</span>
                        </div>
                        <div style={{ fontSize: 14, width: '100%', textAlign: 'left', marginTop: 20, color: '#0B2273' }}>AI智能备课，释放教师创造力</div>
                        <div className={style.preEntryBtn} style={{ marginTop: 15, fontSize: 12, paddingInline: 10 }} onClick={() => handleNavigate(`/home/preparation/${tsId}/${schooldId}`)}>新建</div>
                        <div className={style.preEntryBtn} style={{ marginTop: 10, fontSize: 12, paddingInline: 10 }} onClick={() => handleNavigate('/home/preparationhistory')}>历史记录</div>
                        <Avatar src={PreparationIcon} size={120} className={style.entryCardIcon} />
                    </div>
                    <div className={style.entryStyle}>
                        <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
                            <span style={{ fontSize: 24, fontFamily: 'none', fontWeight: 600 }}>习题</span>
                            <Divider type='vertical' />
                            <span style={{ color: '#727272' }}>exercise</span>
                        </div>
                        <div style={{ fontSize: 14, width: '100%', textAlign: 'left', marginTop: 20, color: '#0B2273' }}>精准题目生成，分层教学无忧</div>
                        <div className={style.preEntryBtn} style={{ marginTop: 25 }} onClick={() => handleNavigate(`/home/homeworkentry/${tsId}/${schooldId}`)}>创建习题</div>
                        <Avatar src={HomeworkIcon} size={120} className={style.entryCardIcon} />
                    </div>
                    <div className={style.entryStyle} >
                        <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
                            <span style={{ fontSize: 24, fontFamily: 'none', fontWeight: 600 }}>班级</span>
                            <Divider type='vertical' />
                            <span style={{ color: '#727272' }}>class</span>
                        </div>
                        <div style={{ fontSize: 14, width: '100%', textAlign: 'left', marginTop: 20, color: '#0B2273' }}>多维学情分析，教学决策有据可依</div>
                        <div className={style.preEntryBtn} style={{ marginTop: 25 }} onClick={() => handleNavigate('/home/classentry')}>进入班级</div>
                        <Avatar src={ClassIcon} size={120} className={style.entryCardIcon} />
                    </div>
                </div>

                <div className={style.cardBox}>
                    <div style={{ width: '100%', height: 'max-content', display: 'flex', marginBottom: 30, justifyContent: 'space-between' }}>
                        <div className={style.progressCard}>
                            <div style={{ width: '100%', padding: 20, background: "linear-gradient( 145deg, #9294E9 18%, #6581ED 100% )" }}>
                                <div style={{ width: '100%', fontSize: 18, fontWeight: 600, textAlign: 'left', marginBottom: 30 }}>备课板</div>
                                <div style={{ width: '100%', fontSize: 24, fontFamily: 'siyuan', marginBottom: 20 }}>{lastDesignName}</div>
                                {lastModify ?
                                    <div>上次修改：{lastModify}</div>
                                    :
                                    <div>暂无修改时间</div>
                                }

                                <div><button className={style.progressCardBtn} onClick={() => navigate(`/texteditor/${dId}`)}> {lastModify ? '继续编辑' : '前往编辑'}</button></div>
                                <Avatar shape='square' src={ProgressIcon} size={180} />
                            </div>
                        </div>

                        <div className={style.dateCard} style={{ width: '70%' }}>
                            <div className={style.toolBoxHeader}>
                                工具箱
                            </div>
                            <div className={style.toolBoxContent}>
                                <div className={style.featureGrid}>
                                    {/* 知识图谱卡片 */}
                                    <div
                                        onClick={() => navigate(`/graph/${tsId}/${schooldId}`)}
                                        className={style.featureCard}
                                        style={{ background: 'linear-gradient(114deg, #7184ffda 0%, #49d7ff 28%,#ffeddb 100%)' }}>
                                        <div className={style.featureIcon} style={{ backgroundColor: '#ffffff' }}>
                                            <Avatar src={KnowledgeGra} size={30} shape='square' />
                                        </div>
                                        <div className={style.featureContent}>
                                            <div className={style.featureTitle}>知识图谱</div>
                                            <div className={style.featureDesc}>构建结构化知识体系</div>
                                        </div>
                                    </div>

                                    {/* PPT制作卡片 */}
                                    <div
                                        onClick={() => navigate('/ppt')}
                                        className={style.featureCard}
                                        style={{ background: 'linear-gradient(135deg, #ffa365 0%,#ffe344 50%, #fff7cd 100%)' }}>
                                        <div className={style.featureIcon} style={{ backgroundColor: '#ffffff' }}>
                                            <Avatar src={PPTIcon} size={34} shape='square' />
                                        </div>
                                        <div className={style.featureContent}>
                                            <div className={style.featureTitle}>PPT制作</div>
                                            <div className={style.featureDesc}>创建精美教学课件</div>
                                        </div>
                                    </div>

                                    {/* 视频制作卡片 */}
                                    <div
                                        onClick={() => navigate('/video')}
                                        className={style.featureCard}
                                        style={{ background: 'linear-gradient(135deg, #aff4ff 0%,#9597ff 50%, #e6effb 120%)' }}>
                                        <div className={style.featureIcon} style={{ backgroundColor: '#ffffff' }}>
                                            <Avatar src={VideoIcon} size={30} shape='square' />
                                        </div>
                                        <div className={style.featureContent}>
                                            <div className={style.featureTitle}>视频制作</div>
                                            <div className={style.featureDesc}>制作互动教学视频</div>
                                        </div>
                                    </div>
                                    {/* 视频制作卡片 */}
                                    <div className={style.featureCard}
                                        style={{ background: 'linear-gradient(135deg, #ff8473 0%,#ffb273 50%, #fff9d2 120%)' }}>
                                        <div className={style.featureIcon} style={{ backgroundColor: '#ffffff' }}>
                                            <Avatar src={KnowledgeCardIcon} size={30} shape='square' />
                                        </div>
                                        <div className={style.featureContent}>
                                            <div className={style.featureTitle}>知识卡片</div>
                                            <div className={style.featureDesc}>知识卡片化</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    {/* <div style={{width:'100%',height:'max-content',display:'flex',justifyContent:'space-between'}}>
                <div className={style.resourceCard}></div>
                <div className={style.homeWorkCard}>
                    
                </div>
                </div> */}
                </div>
            </div>

        </div>
    )
}
export default HomeContent
