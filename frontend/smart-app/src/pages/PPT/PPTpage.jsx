import React, { useEffect, useState } from 'react';
import { Button, Card, Pagination, Progress, Modal, List, Spin, Tag, Typography, Avatar } from 'antd';
import { PlusOutlined, ClockCircleOutlined, ArrowLeftOutlined, FilePptOutlined } from '@ant-design/icons';
import styles from './pptPage.module.css';
import { useNavigate } from 'react-router-dom';
import PPTIcon from '../../assets/svg/PPT.svg'
const { Title, Text } = Typography;
import { getPPTList, getDownloadPPT } from '../../apis/preparation'
import { quantum } from 'ldrs'
quantum.register()




const PPTpage = () => {
    const [currentPage, setCurrentPage] = useState(1);
    const [isModalVisible, setIsModalVisible] = useState(false)
    const [open, setOpen] = useState(false)
    const [isGenerating, setIsGenerating] = useState(false)
    const [progress, setProgress] = useState(0);
    const [selectedDesign, setSelectedDesign] = useState(null)
    const [PPTList, setPPTList] = useState([])
    const [designList, setDeignList] = useState([])
    const userToken = localStorage.getItem('authToken')
    const getPPTListFun = async () => {
        const { data } = await getPPTList()
        console.log(data)

        // 分组逻辑：根据 isHavePPT 属性分组
        const pptItems = [];
        const designItems = []

        data.forEach(item => {
            if (item.isHavePPT) {
                pptItems.push(item);
            } else {
                designItems.push(item);
            }
        });

        setPPTList(pptItems);
        setDeignList(designItems);
    }
    //"A. 横向联邦学习  B. 纵向联邦学习  C. 交叉联邦学习  D. 联邦迁移学习"

    useEffect(() => {
        getPPTListFun()
    }, [isGenerating])

    const navigate = useNavigate()


    const pageSize = 4;
    const startIndex = (currentPage - 1) * pageSize
    const currentPPTData = PPTList?.slice(startIndex, startIndex + pageSize)

    const showModal = () => {
        setOpen(true)
    }

    const handleCancel = () => {
        setOpen(false)
        setSelectedDesign(null)
    }

    const handleDesignSelect = (design) => {
        setSelectedDesign(design)
    }

    const handleGeneratePPT = async () => {
        if (!selectedDesign) return
        setIsGenerating(true)
        setProgress(0)
        setOpen(false)
        const { data } = await getDownloadPPT(selectedDesign.tdId)
        setIsGenerating(false)
    }

    //下载PPT事件
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

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <div>
                    <span className={styles.quitIcon} onClick={() => navigate(-1)}>
                        <ArrowLeftOutlined />
                    </span>
                    <span className={styles.title}>PPT制作</span>
                </div>

                <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    className={styles.createButton}
                    onClick={showModal}
                >
                    制作PPT
                </Button>
            </div>

            <div className={styles.subtitle}>
                <div style={{ flex: 1 }}></div>
                <p>共 {PPTList?.length} 个课件</p>
            </div>

            {isGenerating && <div className={styles.progressContainer}>
                <h3>正在生成PPT: {selectedDesign?.name}</h3>
                {/* <Progress
                    percent={progress}
                    strokeColor={{
                        '0%': '#c800fa',
                        '30%': '#fff27e',
                        '100%': '#ff7c1d',
                    }}
                    className={styles.progressBar}
                /> */}
                <p style={{ marginBottom: 16 }}>请稍候，系统正在根据教学设计生成精美PPT...</p>
                <l-quantum
                    size="45"
                    speed="1.75"
                    color="#ff7c1d"
                ></l-quantum>
            </div>}
            <div className={styles.pptList}>
                {currentPPTData?.map(ppt => (
                    <Card
                        key={ppt.tdId}
                        className={styles.pptCard}
                        cover={
                            <div className={styles.cardCover}>
                                <div className={styles.coverImage}>
                                    <Avatar src={PPTIcon} size={80} />

                                </div>
                                <div className={styles.coverFooter}>
                                    <span style={{ color: '#fff', width: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{ppt.pptName}</span>
                                    <span>{ppt.createTime}</span>
                                </div>
                            </div>
                        }
                    >
                        <div className={styles.cardContent}>
                            <Card
                                hoverable
                                className={styles.cardStyle}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <div>
                                        <Text strong style={{ fontSize: 16 }}>{ppt.designName}</Text>
                                        <div style={{ marginTop: 8 }}>
                                            <Text type="secondary">{ppt.designName} | 更新于: {ppt.lastModify ? ppt.lastModify : '暂无修改'}</Text>
                                        </div>
                                    </div>
                                </div>
                            </Card>
                            <div className={styles.cardActions}>
                                <Button type="primary" ghost onClick={() => downloadPPT(ppt.pptUrl, userToken)}>下载</Button>
                            </div>
                        </div>
                    </Card>
                ))}
            </div>

            <div className={styles.pagination}>
                <Pagination
                    current={currentPage}
                    pageSize={pageSize}
                    total={PPTList?.length}
                    onChange={(page) => setCurrentPage(page)}
                    showSizeChanger={false}
                />
            </div>

            <Modal
                title="选择教学设计生成PPT"
                open={open}
                onCancel={handleCancel}
                destroyOnClose={true}
                footer={[
                    <Button key="back" onClick={handleCancel}>
                        取消
                    </Button>,
                    <Button
                        key="submit"
                        type="primary"
                        disabled={!selectedDesign}
                        onClick={handleGeneratePPT}
                    >
                        开始制作
                    </Button>,
                ]}
                width={800}
                className={styles.modal}
            >

                <List
                    grid={{ gutter: 16, column: 2 }}
                    dataSource={designList}
                    renderItem={design => (
                        <List.Item>
                            <Card
                                className={`${styles.designCard} ${selectedDesign?.tdId === design.tdId ? styles.selected : ''}`}
                                onClick={() => handleDesignSelect(design)}
                            >
                                <div className={styles.designHeader}>
                                    <Tag color="blue">{design.subject}</Tag>
                                    <span>{design.grade}</span>
                                </div>
                                <h4 className={styles.designName}>{design.designName}</h4>
                                <div className={styles.designMeta}>
                                    <span><ClockCircleOutlined /> {design.classTime}</span>
                                    <span>创建: {design.createTime}</span>
                                </div>
                                {selectedDesign?.tdId === design.tdId && (
                                    <div className={styles.selectedIndicator}>
                                    </div>
                                )}
                            </Card>
                        </List.Item>
                    )}
                />

            </Modal>
        </div>
    );
};

export default PPTpage;