// videoPage.jsx
import React, { useEffect, useRef, useState } from 'react'
import { Layout, Button, Card, Input, List, Tag, Avatar } from 'antd'
import {
    ClockCircleOutlined,
    PlayCircleOutlined,
    VideoCameraOutlined,
    ArrowLeftOutlined,
    SearchOutlined,
} from '@ant-design/icons'
import style from './coursePage.module.css'
import JoLPlayer from "jol-player"
import NoneCon from '../../assets/svg/暂无内容.svg'
import VideoIcon from '../../assets/svg/视频.svg'
import DescIcon from '../../assets/svg/DescIcon.svg'
import { useNavigate } from 'react-router-dom'
import VideoMaker from './videoMaker'
import { getVideoList, getVideoFile } from '../../apis/video'
import LittleEditor from '@/component/preparation/littleEditor'
import { grid, reuleaux } from 'ldrs'
grid.register()
reuleaux.register()

const { Header, Sider, Content } = Layout;

const makeVideoStyle = {
    boxShadow: '0px 4px 12px #494d853d',
    background: 'linear-gradient(120deg, #54a2fc 0%, #445FFF 100%)',
    color: '#fff'
}

const VedioPlayer = ({ video }) => {
    const [videoUrl, setVideoUrl] = useState('')
    const [loading, setLoading] = useState(false)
    const [current, setCurrent] = useState(1)
    const [videoDesContent, setVideoDesContent] = useState('')
    // 使用useRef保存当前视频URL
    const currentUrlRef = useRef('')

    useEffect(() => {
        // 如果没有视频数据，直接返回
        if (!video || !video.videoUrl) return

        const fetchVideo = async () => {
            setLoading(true)
            console.log('当前描述', video.videoDesc)
            try {
                // 释放之前创建的视频URL
                if (currentUrlRef.current) {
                    URL.revokeObjectURL(currentUrlRef.current)
                }

                // 获取新视频
                const response = await getVideoFile(video.videoUrl)
                const blobUrl = URL.createObjectURL(response)

                // 保存新URL并更新状态
                currentUrlRef.current = blobUrl
                setVideoUrl(blobUrl)
            } catch (error) {
                console.error('视频加载失败:', error)
            } finally {
                setLoading(false)
            }
        }
        setVideoDesContent(video.videoDesc)
        fetchVideo()

        return () => {
            if (currentUrlRef.current) {
                URL.revokeObjectURL(currentUrlRef.current);
            }
        }
    }, [video])

    return (
        <div className={style.videoContainer}>
            <div className={style.videoPlaceholder}>
                <div className={style.videoHeader}>
                    {/* 修改2：使用传入的视频名称 */}
                    <h3><Avatar src={VideoIcon} size={40} style={{ marginRight: 12 }} /> {video?.videoName || '未选择视频'}</h3>
                </div>
                <div style={{ width: '80%', minWidth: 750 }}>
                    <div style={{ width: '100%', maxWidth: '800px' }}>
                        {loading ? (
                            <div className={style.videoLoadingBox}>
                                <l-reuleaux
                                    size="37"
                                    stroke="5"
                                    stroke-length="0.15"
                                    bg-opacity="0.1"
                                    speed="1.2"
                                    color="black"
                                ></l-reuleaux>
                                <p>加载视频中...</p>
                            </div>
                        ) : videoUrl ? (
                            <video
                                controls
                                src={videoUrl}
                                style={{ width: '100%' }}
                            >
                                您的浏览器不支持 video 标签
                            </video>
                        ) : (
                            <div className={style.videoPlaceholderBox}>
                                <Avatar src={NoneCon} size={96} />
                                <p>请从左侧选择视频</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
            <div style={{ marginLeft: 24, marginTop: 24 }}>
                <Avatar src={DescIcon} size={24} style={{ marginRight: 4 }} />
                <span
                    className={current === 1 ? style.videoDesFocus : style.videoDesBtn}
                    onClick={() => setCurrent(1)}>视频描述</span>
                <span
                    className={current === 2 ? style.videoDesFocus : style.videoDesBtn}
                    style={{ marginLeft: 24 }}
                    onClick={() => setCurrent(2)}
                >相关PPT</span>
            </div>
            <div className={style.videoDesBox}>
                {current === 1 &&
                    <div>
                        {video?.videoDesc ?
                            <LittleEditor returnHtml={videoDesContent} /> :
                            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                <Avatar src={NoneCon} size={96} />
                                <span>暂无说明</span>
                            </div>
                        }
                    </div>
                }
                {current === 2 &&
                    <div>
                        {/* {video.videoWithPPTName?
                        <div>{video.videoDesc}</div> : */}
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                            <Avatar src={NoneCon} size={96} />
                            <span>暂无PPT</span>
                        </div>
                        {/* } */}
                    </div>
                }
            </div>
        </div>
    )
}

const VideoPage = () => {
    const navigate = useNavigate()
    const [isShowVideo, setIsShowVideo] = useState(true)
    const [currentVideo, setCurrentVideo] = useState(null)
    const [videoList, setVideoList] = useState([])
    const [loading, setLoading] = useState(true)
    const savedIsMaking = sessionStorage.getItem('isMaking')
    const getVideoListFun = async () => {
        try {
            const { data } = await getVideoList()
            const videoItems = []
            data.forEach(item => {
                if (item.videoUrl) {
                    videoItems.push(item)
                }
            })
            setVideoList(videoItems)
            if (data.length > 0) {
                setCurrentVideo(data[0])
            }
        } catch (error) {
            console.error('获取视频列表失败:', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        getVideoListFun()
    }, [])


    const handleSelectVideo = (video) => {
        setIsShowVideo(true)
        setCurrentVideo(video)
        console.log('当前的视频', video)
    }

    const handleClickMaker = () => {
        setIsShowVideo(false)
        setCurrentVideo({
            vid: 0
        })
    }

    return (
        <Layout className={style.container}>
            <Header className={style.header}>
                <div className={style.quitVideoIcon} onClick={() => navigate(-1)}>
                    <ArrowLeftOutlined />
                </div>
                <span style={{ fontSize: 16, fontFamily: 'siyuan', lineHeight: 0 }}>备课视频</span>
            </Header>

            <Layout className={style.mainContent}>
                <Sider className={style.sidebar} width={360}>
                    <Card title="视频列表" className={style.videoListCard}>
                        {loading ? (
                            <div style={{ display: 'flex', justifyContent: 'center', padding: '40px 0' }}>
                                <l-grid size="46" speed="2.5" color="#435fff"></l-grid>
                            </div>
                        ) : videoList.length > 0 ? (
                            <List
                                itemLayout="horizontal"
                                dataSource={videoList}
                                pagination={{
                                    pageSize: 4,
                                    position: 'bottom',
                                    align: 'center',
                                    itemRender: (current, type, originalElement) => {
                                        if (type === 'prev') return <a>上一页</a>
                                        if (type === 'next') return <a>下一页</a>
                                        if (type === 'page') return <a>{current}</a>
                                        return originalElement
                                    },
                                }}
                                renderItem={(item) => (
                                    <List.Item
                                        onClick={() => handleSelectVideo(item)}
                                        className={`${style.videoListItem} ${currentVideo?.vid === item.vid ? style.currentVideo : ''}`}
                                    >
                                        <div className={style.thumbnailContainer}>
                                            <div className={`${style.thumbnail}`} style={{ background: 'linear-gradient(135deg, #e3d2ff 0%,#a7c0ff 60%, #bbb2ff 100%)' }}>
                                                <PlayCircleOutlined className={style.playIcon} />
                                            </div>
                                        </div>
                                        <div className={style.videoInfo}>
                                            <div className={style.videoTitle}>
                                                {item.videoName}
                                            </div>
                                            <div className={style.videoMeta}>
                                                <span className={style.duration}>
                                                    视频大小： {item.videoSize}
                                                </span>
                                            </div>
                                        </div>
                                    </List.Item>
                                )}
                            />
                        ) : (
                            <div style={{ padding: '40px 0', textAlign: 'center' }}>
                                <Avatar src={NoneCon} size={96} />
                                <p>暂无视频内容</p>
                            </div>
                        )}
                    </Card>
                    <div className={style.makeVideoBtnBox} style={isShowVideo ? null : makeVideoStyle} onClick={() => handleClickMaker()}>
                        <span> <VideoCameraOutlined /> 视频制作</span>
                    </div>
                    {savedIsMaking && (
                        <div className={style.makingBox}>
                            <span><l-grid size="46" speed="2.5" color="#435fff"></l-grid></span>
                            <span className={style.makingText}>视频制作中</span>
                        </div>
                    )}
                    <div style={{ width: '100%', height: 12 }}></div>
                </Sider>

                <Content className={style.content}>
                    {/* 修改6：传递当前视频对象 */}
                    {isShowVideo ? (
                        <VedioPlayer video={currentVideo} />
                    ) : (
                        <div className={style.videoMaker}>
                            <VideoMaker />
                        </div>
                    )}
                </Content>
            </Layout>
        </Layout>
    );
};

export default VideoPage