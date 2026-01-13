import React, { useEffect, useRef, useState } from 'react'
import ForceGraph2D from 'react-force-graph-2d'
import { Avatar, Button, Card, Drawer, Empty, Input, List, message, Popover, Space, Tree, Typography } from 'antd'
import style from './graphStyle.module.css'
import NoneResource from '../../assets/svg/暂无内容.svg'
import { SearchOutlined, MenuUnfoldOutlined, ReadOutlined, EditOutlined, PlaySquareOutlined, ArrowLeftOutlined, PlusCircleOutlined, DownOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import PPTLogo from '../../assets/svg/PPT.svg'
import { getGraphCatalogue } from '../../apis/preparation'
import PageLoadingComponent from '../../component/Loading/pageLoadingComponent'
import { getPointDesign, getPointPPT, getPointHomework, getPointPreview, getPointVideo } from '../../apis/graph'

const btnFocusStyle = {
    color: '#007BFF',
    background: '#fff'
}



const { Text } = Typography;

const KnowledgeGraph = ({ resData }) => {
    let { tsId, schoolId } = useParams()
    const graphRef = useRef();
    const [selectedNode, setSelectedNode] = useState('')
    const [showDetails, setShowDetails] = useState(false)
    const [hoverNode, setHoverNode] = useState(null)
    const [currentPage, setCurrentPage] = useState(1)
    const [searchTerm, setSearchTerm] = useState('')
    const [isShowCata, setIsShowCata] = useState(false)
    const [catalogueData, setCatalogueData] = useState([])
    const [pointCout, setPointCout] = useState("暂无数据")
    const [isDataLoading, setIsDataLoading] = useState(true)
    const [graphData, setGraphData] = useState({ nodes: [], links: [] })
    const [pptList, setPPTList] = useState([])
    const [designList, setDeignList] = useState([])
    const [homeworkList, setHomeworkList] = useState()
    const [preWorkList, setPreWorkList] = useState([])
    const [videoList, setVideoList] = useState([])
    const userToken = localStorage.getItem('authToken')
    const uid = sessionStorage.getItem('uid')

    const navigate = useNavigate()

    const getCatalogueData = async () => {
        let { data } = await getGraphCatalogue(tsId, schoolId)
        setCatalogueData(data)
    }

    const getDesignListFun = async (title) => {
        let { data } = await getPointDesign(title)
        setDeignList(data)
    }
    const getPPTListFun = async (title) => {
        let { data } = await getPointPPT(title)

        const pptItems = []
        data.forEach(item => {
            if (item.pptUrl) {
                pptItems.push(item)
            }
        })
        setPPTList(pptItems)
    }

    const getPreList = async (title, uid) => {
        let { data } = await getPointPreview(title, uid)
        setPreWorkList(data)
    }

    const getHomeworkList = async (title, uid) => {
        let { data } = await getPointHomework(title, uid)
        setHomeworkList(data)
    }

    const getVideoList = async (title) => {
        let { data } = await getPointVideo(title)
        const videoItems = []
        data.forEach(item => {
            if (item.videoUrl) {
                videoItems.push(item)
            }
        })
        setVideoList(videoItems)

    }


    const downloadPPT = async (pptPath, token) => {
        try {
            // 从路径中提取文件名
            const fileName = pptPath.split('/').pop().split('\\').pop() || 'presentation.pptx';

            // 构建请求URL
            const url = `http://localhost:8080/teachDesign/ppt?pptUrl=${encodeURIComponent(pptPath)}`;

            // 发起请求
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'token': token,
                    'Accept': 'application/octet-stream'
                }
            });

            // 检查响应状态
            if (!response.ok) {
                throw new Error(`服务器返回错误: ${response.status} ${response.statusText}`)
            }

            // 获取文件数据
            const blob = await response.blob()

            // 创建下载链接
            const downloadUrl = URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = downloadUrl;
            link.download = fileName;
            link.style.display = 'none'

            // 触发下载
            document.body.appendChild(link)
            link.click()

            // 清理资源
            document.body.removeChild(link)
            setTimeout(() => URL.revokeObjectURL(downloadUrl), 100)

            return true
        } catch (error) {
            console.error('文件下载失败:', error)
            throw new Error(`下载失败: ${error.message}`)
        }
    }


    const initGraphData = () => {
        if (resData) {
            const processedData = {
                links: resData.link || [],
                nodes: resData.nodes || []
            };
            setGraphData(processedData);
        } else {
            // 如果resData未定义，设置为空状态
            setGraphData({ nodes: [], links: [] });
        }
    }

    useEffect(() => {
        const fetchData = async () => {
            setIsDataLoading(true)
            await getCatalogueData()
            initGraphData()
            setPointCout(resData.pointsNum)
            setTimeout(() => {
                setIsDataLoading(false)
            }, 1800);
        }

        fetchData()
    }, [resData]) // 添加依赖项，当resData或pointData变化时重新加载



    const onClose = () => {
        setShowDetails(false);
    };

    const handleHover = (node) => {
        setHoverNode(node)
    }

    // 节点点击处理函数
    const handleNodeClick = (node) => {
        setSelectedNode(node)
        setShowDetails(true)
        getDesignListFun(node.topTitle)
        getPPTListFun(node.topTitle)
        getPreList(node.topTitle, uid)
        getHomeworkList(node.topTitle, uid)
        getVideoList(node.topTitle)
    }

    // 背景点击处理函数
    const handleBackgroundClick = () => {
        setShowDetails(false);
    };

    // 根据节点层级计算半径大小（level越大半径越小）
    const calculateNodeRadius = (level) => {
        const baseSize = 15;
        const sizeMultiplier = 0.8;
        return baseSize * Math.pow(sizeMultiplier, level + 2);
    }



    // 根据层级获取节点颜色
    const getNodeColor = (level) => {
        const colors = {
            1: '#45d0bd',
            2: '#fb8c00',
            3: '#a2b1fb'
        }
        return colors[level] || '#76b7b2'; // 默认颜色
    }
    const getNodeColor2 = (level) => {
        const colors = {
            1: '#79dacd',
            2: '#f8b96c',
            3: '#c0caf3'
        }
        return colors[level] || '#76b7b2'; // 默认颜色
    }
    const getHoverNodeColor = (level) => {
        const colors = {
            1: '#007a6a',
            2: '#e65100',
            3: '#7472fe'
        };
        return colors[level] || '#76b7b2'; // 默认颜色
    }
    const getHoverNodeColor2 = (level) => {
        const colors = {
            1: '#33b6a4',
            2: '#f87027',
            3: '#a19fff'
        };
        return colors[level] || '#76b7b2'; // 默认颜色
    }

    const handleDrawerBtnClick = (e) => {
        setCurrentPage(e)
    }

    // 添加节点阴影效果
    const addNodeShadow = (ctx, node, radius) => {
        const shadowColor = getNodeColor(node.level);
        const shadowOpacity = '66'; // 40% 透明度
        const shadowBlur = 10;
        ctx.shadowColor = `${shadowColor}${shadowOpacity}`;
        ctx.shadowBlur = shadowBlur;
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 4;
    };


    const treeData = [
        {
            title: '人工智能通识课',
            key: '1',
            children: [
                ...catalogueData
            ]
        }
    ]

    const handleShowCatalogue = () => {
        setIsShowCata(true)
    }
    const handleCloseCatalogue = () => {
        setIsShowCata(false)
    }

    function renderTag(e) {
        switch (e) {
            case 0:
                return <span className={style.noTag}>未发布</span>;
            case 1:
                return <span className={style.workTag}>进行中</span>;
            case -1:
                return <span className={style.falseTag}>已结束</span>;

            default:
                break;
        }
    }



    return (
        <>
            {isDataLoading ? <PageLoadingComponent /> :
                <div style={{
                    position: 'relative',
                    width: '100%',
                    height: '100vh',
                    background: '#F6F7FB',
                    borderRadius: '8px',
                    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.08)',
                    overflow: 'hidden',
                    fontFamily: "'Segoe UI', 'PingFang SC', sans-serif"
                }}>
                    <div className={style.graphHeader}>
                        <span className={style.quitIcon} onClick={() => navigate(-1)}>
                            <ArrowLeftOutlined />
                        </span>
                        <div style={{ paddingBlock: 5, paddingInline: 10, borderRadius: 5, border: '1px solid #3a93fe', background: "#ecf2ff" }}>
                            <div className={style.titleTextBox}>
                                人工智能知识图谱
                            </div>
                        </div>

                    </div>
                    {isShowCata &&
                        <div className={style.catalogueBox}>
                            <div className={style.catalogueBoxHeader}>
                                <span>目录</span>
                                <span style={{ flex: 1 }}></span>
                                <span className={style.closeBtn} onClick={() => handleCloseCatalogue()}>×</span>
                            </div>
                            <div className={style.catalogueContent}>
                                <Tree
                                    showLine
                                    switcherIcon={<DownOutlined />}
                                    treeData={treeData}
                                    titleRender={item => {
                                        return item.title
                                    }}
                                />
                            </div>

                        </div>
                    }


                    <div className={style.graphSider}>
                        <div style={{ color: '#656A72' }}>知识点总数</div>
                        <div style={{ fontSize: 30, fontWeight: 600 }}>{pointCout}</div>
                        <Input
                            prefix={<SearchOutlined />}
                            style={{ color: '#656A72', marginTop: 10, marginBottom: 10 }}
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                        <div>
                            <div style={{ marginTop: 10 }} ><span className={style.graphDot} style={{ background: '#45d0bd' }} />课程</div>
                            <div style={{ marginTop: 10 }} ><span className={style.graphDot} style={{ background: '#fb8c00' }} />章节</div>
                            <div style={{ marginTop: 10 }} ><span className={style.graphDot} style={{ background: '#a2b1fb' }} />知识点</div>
                        </div>
                        <div style={{ borderBottom: '2px solid #eaeaea', height: 30, width: '100%' }} />
                        <div className={style.graphMenuBtn} onClick={() => handleShowCatalogue()}><MenuUnfoldOutlined /> 目录</div>
                    </div>

                    <Popover content="添加新知识点" >
                        <div className={style.addKnowledge}>
                            <PlusCircleOutlined style={{ fontSize: 20, marginLeft: 10 }} />
                        </div>
                    </Popover>


                    <ForceGraph2D
                        ref={graphRef}
                        graphData={graphData}
                        nodeAutoColorBy="level"
                        nodeVal={node => calculateNodeRadius(node.level)}
                        linkCurvature={0.05} // 减小曲线弧度
                        linkColor={() => 'rgba(120, 120, 120, 0.25)'} // 更淡的连线颜色

                        d3VelocityDecay={0.5} // 降低速度衰减，使布局更稳定
                        cooldownTicks={100} // 延长冷却时间

                        onNodeHover={(node) => {
                            handleHover(node) // 更新悬停节点
                        }}

                        linkWidth={1.5}

                        nodeCanvasObject={(node, ctx, globalScale) => {
                            const radius = calculateNodeRadius(node.level);
                            const maxWidth = radius * 5; // 最大文字宽度
                            const ellipsis = "...";
                            // const label = node.topTitle;
                            const fontSize = Math.min(8, radius * 0.7);


                            // 确保节点位置是有效数字
                            if (!isFinite(node.x) || !isFinite(node.y)) return

                            // 绘制节点阴影
                            ctx.beginPath();
                            ctx.arc(node.x, node.y, radius, 0, 2 * Math.PI, false);
                            addNodeShadow(ctx, node, radius);
                            ctx.fillStyle = 'transparent';
                            ctx.fill();
                            // clearShadow(ctx);

                            // 绘制节点主体
                            ctx.beginPath();
                            ctx.arc(node.x, node.y, radius, 0, 2 * Math.PI, false);

                            // 创建球形渐变效果
                            const gradient = ctx.createRadialGradient(
                                node.x - radius / 3, node.y - radius / 3, radius / 10,
                                node.x, node.y, radius
                            );
                            const baseColor = getNodeColor(node.level)
                            const gradientColor = getNodeColor2(node.level)

                            gradient.addColorStop(0, '#ffffff');
                            gradient.addColorStop(0.5, gradientColor)
                            gradient.addColorStop(1, baseColor)

                            const hoverBaseColor = getHoverNodeColor(node.level)
                            const gradientHoverColor = getHoverNodeColor2(node.level)

                            const gradientHover = ctx.createRadialGradient(
                                node.x - radius / 3, node.y - radius / 3, radius / 10,
                                node.x, node.y, radius
                            );

                            gradientHover.addColorStop(0, '#ffffff')
                            gradientHover.addColorStop(0.7, gradientHoverColor)
                            gradientHover.addColorStop(1, hoverBaseColor)



                            ctx.fillStyle = node === hoverNode ? gradientHover : gradient
                            // ctx.fillStyle = baseColor
                            ctx.fill()

                            // 测量文字宽度
                            ctx.font = `${fontSize}px 'Segoe UI', sans-serif`;
                            const fullText = node.topTitle;
                            let displayText = fullText;

                            // 如果文字过长，截断并添加 ...
                            if (ctx.measureText(fullText).width > maxWidth) {
                                let truncated = "";
                                for (let i = 0; i < fullText.length; i++) {
                                    truncated = fullText.substring(0, i);
                                    if (ctx.measureText(truncated + ellipsis).width > maxWidth) {
                                        displayText = truncated.slice(0, -1) + ellipsis;
                                        break;
                                    }
                                }
                            }
                            ctx.textAlign = 'center'
                            ctx.textBaseline = 'top'
                            ctx.fillStyle = '#444'
                            ctx.fillText(displayText, node.x, node.y + radius + 2);
                        }}
                        onNodeClick={handleNodeClick}
                        onBackgroundClick={handleBackgroundClick}
                        // 添加布局稳定回调
                        onEngineStop={() => {
                            graphRef.current.zoom(4, 600);
                        }}
                    />

                    <Drawer
                        placement="right"
                        width={600}
                        onClose={onClose}
                        open={showDetails}
                        closable={false}
                    >
                        <div className={style.drawerBox}>
                            <h3 style={{ fontSize: 24, color: '#333333', marginLeft: 24 }}>{selectedNode.topTitle}</h3>
                            <div className={style.graphBtnBox}>
                                <div style={currentPage === 1 ? btnFocusStyle : null} className={style.drawerBtn} onClick={() => handleDrawerBtnClick(1)}><ReadOutlined /> 知识点详情</div>
                                <div style={currentPage === 2 ? btnFocusStyle : null} className={style.drawerBtn} onClick={() => handleDrawerBtnClick(2)}><ReadOutlined /> 习题</div>
                                <div style={currentPage === 3 ? btnFocusStyle : null} className={style.drawerBtn} onClick={() => handleDrawerBtnClick(3)}><PlaySquareOutlined /> 视频资源</div>
                            </div>
                            <div className={style.contentBox}>
                                {currentPage === 1 && <>
                                    <div style={{ paddingTop: 20, paddingBottom: 10, fontFamily: 'siyuan', fontSize: 14 }}>教学设计</div>
                                    <div style={{ width: '100%', paddingInline: 10, paddingBlock: 10, borderRadius: 10, background: '#F6F7FB' }}>
                                        {
                                            designList ?
                                                <List
                                                    itemLayout="vertical"
                                                    dataSource={designList}
                                                    locale={{ emptyText: "暂无数据" }}
                                                    renderItem={item => (
                                                        <Card
                                                            hoverable
                                                            className={style.cardStyle}
                                                        >
                                                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                                                <div>
                                                                    <Text strong style={{ fontSize: 16 }}>{item.designName}</Text>
                                                                    <div style={{ marginTop: 8 }}>
                                                                        <Text type="secondary">{item.authorName} | 更新于: {item.lastModify ? item.lastModify : '暂未修改'}</Text>
                                                                    </div>
                                                                </div>
                                                                <Button
                                                                    type="primary"
                                                                    icon={<EditOutlined />}
                                                                    onClick={() => navigate(`/texteditor/${item.tdId}`)}
                                                                >
                                                                    编辑
                                                                </Button>
                                                            </div>
                                                        </Card>
                                                    )}
                                                />
                                                :
                                                <div style={{ width: '100%', height: 100, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                    <Avatar src={NoneResource} size={100} />
                                                    <div>暂无资源</div>
                                                </div>
                                        }

                                    </div>
                                    <div style={{ paddingTop: 20, paddingBottom: 10, fontFamily: 'siyuan', fontSize: 14 }}>教学PPT</div>
                                    <div className={style.pptBar} style={{ borderRadius: 10 }}>
                                        {
                                            pptList ? pptList.map((item, index) => (
                                                <div className={style.pptCard} key={item.pptId}>
                                                    <Avatar src={PPTLogo} size={48} />
                                                    <div style={{ flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.pptName}</div>
                                                    <span className={style.pptCardBtn} onClick={() => downloadPPT(item.pptUrl, userToken)}>下载</span>
                                                </div>
                                            ))
                                                :
                                                <div style={{ width: '100%', height: 100, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                    <Avatar src={NoneResource} size={100} />
                                                    <div>暂无资源</div>
                                                </div>
                                        }

                                    </div>
                                    <div style={{ paddingTop: 20, paddingBottom: 10, fontFamily: 'siyuan', fontSize: 14 }}>知识点描述</div>
                                    <div style={{ width: '100%', minHeight: 100, paddingInline: 24, paddingBlock: 10, borderRadius: 10, background: '#F6F7FB', color: '#333' }}>
                                        {selectedNode.content}
                                    </div>
                                </>}
                                {currentPage === 2 && <>
                                    <div style={{ paddingTop: 20, paddingBottom: 10, fontFamily: 'siyuan', fontSize: 14 }}>预习任务列表</div>
                                    <div style={{ width: '100%', paddingInline: 10, paddingBlock: 10, borderRadius: 10, background: '#F6F7FB' }}>
                                        {preWorkList ?
                                            <div>
                                                {preWorkList?.map((item, index) => (
                                                    <div className={style.workHistoryItem} key={index}>
                                                        <span style={{ fontFamily: 'siyuan', fontSize: 15, width: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.previewName}</span>
                                                        {renderTag(item.active)}
                                                        <span style={{ fontWeight: 600, color: '#808080', width: 40 }}>{item.questionsGrade}分</span>
                                                        <div style={{ width: 80, display: 'flex' }}>
                                                            <button className={style.detalBtn} onClick={() => navigate(`/preview/${item.ptId}`)}>查看作业</button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                            :
                                            <div style={{ width: '100%', height: 100, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                <Avatar src={NoneResource} size={100} />
                                                <div>暂无资源</div>
                                            </div>
                                        }
                                    </div>

                                    <div style={{ paddingTop: 20, paddingBottom: 10, fontFamily: 'siyuan', fontSize: 14 }}>作业列表</div>
                                    <div style={{ width: '100%', paddingInline: 10, paddingBlock: 10, borderRadius: 10, background: '#F6F7FB' }}>
                                        {homeworkList ?
                                            <div>
                                                {homeworkList?.map((item, index) => (
                                                    <div className={style.workHistoryItem} key={index}>
                                                        <span style={{ fontFamily: 'siyuan', fontSize: 15, width: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.hname}</span>
                                                        {renderTag(item.state)}
                                                        <span style={{ fontWeight: 600, color: '#808080', width: 40 }}>{item.score}分</span>
                                                        <div style={{ width: 80, display: 'flex' }}>
                                                            <button className={style.detalBtn} onClick={() => navigate(`/homework/${item.hid}`)}>查看作业</button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                            :
                                            <div style={{ width: '100%', height: 100, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                <Avatar src={NoneResource} size={100} />
                                                <div>暂无资源</div>
                                            </div>

                                        }
                                    </div>
                                </>}
                                {currentPage === 3 && <>
                                    <div style={{ paddingTop: 20, paddingBottom: 10, fontFamily: 'siyuan', fontSize: 14 }}>课程视频</div>
                                    <div style={{ width: '100%', paddingInline: 10, paddingBlock: 10, borderRadius: 10, background: '#F6F7FB' }}>

                                        {videoList ?
                                            <List
                                                style={{ cursor: 'pointer' }}
                                                itemLayout="horizontal"
                                                dataSource={videoList}
                                                renderItem={(item) => (
                                                    <List.Item
                                                    // onClick={() => handleSelectVideo(item)}
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
                                            :
                                            <div style={{ width: '100%', height: 100, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                <Avatar src={NoneResource} size={100} />
                                                <div>暂无资源</div>
                                            </div>
                                        }

                                    </div>


                                </>}
                            </div>
                        </div>


                    </Drawer>

                </div>
            }
        </>
    );
}

export default KnowledgeGraph;


