import React, { useEffect, useState } from 'react'
import { Button, message, Steps, Radio, Upload, Pagination, Avatar } from 'antd'
import { CheckCard } from '@ant-design/pro-components'
import style from './coursePage.module.css'
import { UploadOutlined } from '@ant-design/icons'
import DesignIcon from '../../assets/png/13.png'
import PPTLogo from '../../assets/svg/PPT.svg'
import ProgressBar from './makeLoading'
import { getDesignList, postVideoConfig, getVideoMakingProgress } from '../../apis/video'


const steps = [
    {
        title: '第一步',
    },
    {
        title: '第二步',
    },
    {
        title: '第三步'
    },
    {
        title: '最后一步',
    }
]

const SelectDesign = ({ formData, updateFormData }) => {
    const [currentPage, setCurrentPage] = useState(1)
    const [designList, setDesignList] = useState([])
    const uid = sessionStorage.getItem('uid')

    const getDesignListFun = async (uid) => {
        const { data } = await getDesignList(uid)
        console.log(data)
        const noVideoDesign = []

        data.forEach(i => {
            if (!i.isHaveVideo) {
                noVideoDesign.push(i)
            }
        })

        setDesignList(noVideoDesign)
    }

    useEffect(() => {
        getDesignListFun(uid)
    }, [])

    const getCurrentPageData = () => {
        const startIndex = (currentPage - 1) * 9;
        const endIndex = startIndex + 9;
        return designList.slice(startIndex, endIndex)
    };

    // 处理分页变化
    const handlePageChange = (page) => {
        setCurrentPage(page)
    }
    function handleClick(e) {
        updateFormData('tdId', e)
    }

    return (
        <>
            <div className={style.selectPPTBox}>
                <div style={{ fontSize: 24, fontFamily: 'siyuan' }}>选择教学设计</div>
                <div className={style.pptItemBox}>
                    <CheckCard.Group
                        onChange={(value) => handleClick(value)}
                    >
                        {getCurrentPageData().map((item, index) => (
                            <CheckCard
                                key={item.tdId}
                                className={style.designItem}
                                style={{ width: 240, fontSize: 12 }}
                                size={'small'}
                                title={item.designName}
                                value={item.tdId}
                                avatar={DesignIcon}
                                description={item.createTime}
                            />
                        ))}
                    </CheckCard.Group>
                </div>
                <div className={style.paginationContainer}>
                    <Pagination
                        current={currentPage}
                        pageSize={9}
                        total={designList.length}
                        onChange={handlePageChange}
                        showSizeChanger={false}
                        className={style.customPagination}
                    />
                </div>
            </div>
        </>
    )
}

const SelectPPT = ({ formData, updateFormData }) => {
    const [fileStatus, setFileStatus] = useState({
        uploading: false,
        uploaded: false
    })
    const handleFileUpload = (file) => {
        // 检查文件类型
        const validExtensions = ['.ppt', '.pptx'];
        const fileExtension = file.name.slice(file.name.lastIndexOf('.')).toLowerCase();

        if (!validExtensions.includes(fileExtension)) {
            message.error('只能上传PPT文件 (.ppt, .pptx)');
            return false;
        }

        // 检查文件大小 (最大50MB)
        const maxSize = 50 * 1024 * 1024; // 50MB
        if (file.size > maxSize) {
            message.error('文件大小不能超过50MB')
            return false;
        }

        setFileStatus({ uploading: true, uploaded: false })

        // 直接存储文件对象
        updateFormData('pptFile', file);
        message.success(`${file.name} 准备上传`);
        setFileStatus({ uploading: false, uploaded: true })

        return false;
    }
    return (
        <>
            <div className={style.selectPPTBox}>
                <div style={{ fontSize: 24, fontFamily: 'siyuan' }}>选择PPT</div>
                <div className={style.pptItemBox}>
                    <div style={{ margin: 'auto', width: 'max-content' }}>
                        <Upload
                            accept=".ppt,.pptx"
                            beforeUpload={handleFileUpload}
                            showUploadList={false}
                        >
                            <Button>点击选择</Button>
                        </Upload>
                    </div>
                    {formData.pptFile && (
                        <div style={{ width: 'max-content', margin: 'auto', marginTop: 24 }}>
                            <CheckCard
                                style={{ width: 500, fontSize: 12 }}
                                size={'small'}
                                title={formData.pptFile.name}
                                avatar={PPTLogo}
                                description={`${(formData.pptFile.size / 1024 / 1024).toFixed(3)}MB`}
                            />
                        </div>
                    )}
                </div>
            </div>
        </>
    )
}

const ConfigBox = ({ formData, updateFormData }) => {

    return (
        <>
            <div className={style.selectPPTBox}>
                <div style={{ fontSize: 24, fontFamily: 'siyuan' }}>参数配置</div>
                <div className={style.stepContent}>
                    <div className={style.formSection}>
                        <h3>动作配置</h3>
                        <Radio.Group
                            onChange={(e) => updateFormData('hasAction', e.target.value)}
                            value={formData.hasAction}
                        >
                            <Radio.Button value={true} className={style.smallRadio}>有动作</Radio.Button>
                            <Radio.Button value={false} className={style.smallRadio}>无动作</Radio.Button>
                        </Radio.Group>
                    </div>

                    <div className={style.divider} />

                    <div className={style.formSection}>
                        <h3>增强设置</h3>
                        <div className={style.enhanceGroup}>
                            <div className={style.enhanceItem}>
                                <label>脸部增强</label>
                                <Radio.Group
                                    onChange={(e) => updateFormData('faceEnhance', e.target.value)}
                                    value={formData.faceEnhance}
                                >
                                    <Radio.Button value={true} className={style.smallRadio}>是</Radio.Button>
                                    <Radio.Button value={false} className={style.smallRadio}>否</Radio.Button>
                                </Radio.Group>
                            </div>

                            <div className={style.enhanceItem}>
                                <label>动作增强</label>
                                <Radio.Group
                                    onChange={(e) => updateFormData('actionEnhance', e.target.value)}
                                    value={formData.actionEnhance}
                                >
                                    <Radio.Button value={true} className={style.smallRadio}>是</Radio.Button>
                                    <Radio.Button value={false} className={style.smallRadio}>否</Radio.Button>
                                </Radio.Group>
                            </div>
                        </div>
                    </div>
                    <div className={style.divider} />
                    <div className={style.formSection}>
                        <h3>声音配置</h3>
                        <Radio.Group
                            onChange={(e) => updateFormData('voice', e.target.value)}
                            value={formData.voice}
                            className={style.voiceGroup}
                        >
                            <div className={style.voiceOption}>
                                <Radio.Button value={'0'} className={style.smallRadio}>
                                    <span>男性</span>
                                </Radio.Button>
                            </div>

                            <div className={style.voiceOption}>
                                <Radio.Button value={'1'} className={style.smallRadio}>
                                    <span>女性</span>
                                </Radio.Button>
                            </div>
                        </Radio.Group>
                    </div>
                </div>
            </div>
        </>
    )
}

const ImageConfig = ({ formData, updateFormData }) => {
    const [previewUrl, setPreviewUrl] = useState(null);
    const [fileStatus, setFileStatus] = useState({
        uploading: false,
        uploaded: false
    });

    useEffect(() => {
        return () => {
            if (previewUrl) {
                URL.revokeObjectURL(previewUrl);
            }
        };
    }, [previewUrl]);

    const handleUpload = (file) => {
        const isVideo = file.type === 'video/mp4';
        const isImage = file.type.startsWith('image/');
        const maxSize = 100 * 1024 * 1024; // 100MB

        if (!isVideo && !isImage) {
            message.error('只能上传MP4视频或图片文件');
            return false;
        }

        if (file.size > maxSize) {
            message.error('文件大小不能超过100MB');
            return false;
        }

        // 生成预览URL
        const url = URL.createObjectURL(file);
        setPreviewUrl(url);
        setFileStatus({ uploading: true, uploaded: false });

        // 直接存储文件对象
        updateFormData('avatar', file);
        setFileStatus({ uploading: false, uploaded: true });

        return false;
    }


    return (
        <>
            <div style={{ fontSize: 24, fontFamily: 'siyuan', textAlign: 'center' }}>数字人形象配置</div>

            <div className={style.selectImageBox}>

                <div className={style.formSection} style={{ flex: 1 }}>
                    <Upload
                        accept=".mp4,image/*"
                        beforeUpload={handleUpload}
                        showUploadList={false}
                    >
                        <div className={style.uploadArea}>
                            <UploadOutlined className={style.uploadIcon} />
                            <p className={style.uploadText}>点击或拖拽文件到此处上传</p>
                            <p className={style.uploadHint}>支持 MP4、JPG、PNG 格式，最大100MB</p>
                        </div>
                        <div className={style.uploadHint}>支持上传MP4视频或图片文件</div>
                    </Upload>
                </div>
                <div style={{ flex: 1 }}>
                    {formData.avatar && (
                        <div className={style.uploadPreview}>
                            {formData.avatar.type.startsWith('image/') ? (
                                <img
                                    src={URL.createObjectURL(formData.avatar)}
                                    alt="预览"
                                    className={style.previewImage}
                                />
                            ) : (
                                <video className={style.previewVideo} controls>
                                    <source src={URL.createObjectURL(formData.avatar)} type="video/mp4" />
                                </video>
                            )}
                            <p className={style.fileName}>{formData.avatar.name}</p>
                        </div>
                    )}
                </div>

            </div>
        </>
    )
}

const VideoMaker = () => {
    const [messageApi, contextHolder] = message.useMessage()
    const [current, setCurrent] = useState(0)
    const [makingProgress, setMakingProgress] = useState(-1) // -1:未开始, 0-100:进度, 101:完成
    const [isMaking, setIsMaking] = useState(false)
    const [progressInterval, setProgressInterval] = useState(null)
    const [formData, setFormData] = useState({
        pptFile: null, //PPT文件
        avatar: null, //数字人形象文件  
    })
    const [config, setConfig] = useState({
        hasAction: null, //是否有动作
        faceEnhance: null, //脸部增强 
        actionEnhance: null, //动作增强
        voice: null, //声音性别
        tdId: null, //教学设计ID
    })

    // 在 useEffect 中添加初始化状态检查
    useEffect(() => {
        // 检查是否有保存的制作状态
        const savedIsMaking = sessionStorage.getItem('isMaking')
        const savedProgress = sessionStorage.getItem('makingProgress');

        if (savedIsMaking === 'true') {
            setIsMaking(true);
            setMakingProgress(Number(savedProgress) || 0);

            // 如果进度未完成，则重新开始轮询
            if (savedProgress < 100) {
                startProgressPolling();
            }
        }
    }, []); // 空依赖数组，只在组件挂载时执行一次

    // 清理轮询
    useEffect(() => {
        return () => {
            if (progressInterval) {
                clearInterval(progressInterval);
            }

            // 如果制作完成，清理存储
            if (makingProgress >= 100) {
                sessionStorage.removeItem('isMaking');
                sessionStorage.removeItem('makingProgress');
            }
        };
    }, [progressInterval, makingProgress])
    // 轮询函数
    // 修改 startProgressPolling 函数
    const startProgressPolling = () => {
        // 保存制作状态
        sessionStorage.setItem('isMaking', 'true');
        sessionStorage.setItem('makingProgress', makingProgress.toString());

        const interval = setInterval(async () => {
            try {
                const { data } = await getVideoMakingProgress();

                if (data === null) {
                    // 没有制作任务
                    setMakingProgress(-1);
                    setIsMaking(false);
                    sessionStorage.removeItem('isMaking');
                    sessionStorage.removeItem('makingProgress');
                } else if (data === -1.0) {
                    // 未初始化
                    setMakingProgress(0);
                    sessionStorage.setItem('makingProgress', '0');
                } else {
                    // 正常进度
                    setMakingProgress(data);
                    sessionStorage.setItem('makingProgress', data.toString());

                    // 进度完成时停止轮询
                    if (data >= 100) {
                        clearInterval(interval)
                        setMakingProgress(101) // 标记为完成
                        setIsMaking(false)
                        sessionStorage.removeItem('isMaking')
                        sessionStorage.removeItem('makingProgress')
                    }
                }
            } catch (error) {
                console.error('获取进度失败:', error)
            }
        }, 2000)

        setProgressInterval(interval)
    }

    const updateFormData = (key, value) => {
        setFormData(prev => ({
            ...prev,
            [key]: value
        }))
    }
    const updateConfig = (key, value) => {
        setConfig(prev => ({
            ...prev,
            [key]: value
        }))
    }

    const next = () => {
        setCurrent(current + 1);
    }

    const prev = () => {
        setCurrent(current - 1);
    }

    const items = steps.map(item => ({ key: item.title, title: item.title }))

    function renderSteps() {
        return (
            <>
                <div style={{ display: current === 0 ? 'block' : 'none' }}>
                    <SelectDesign
                        formData={config}
                        updateFormData={updateConfig}
                    />
                </div>
                <div style={{ display: current === 1 ? 'block' : 'none' }}>
                    <SelectPPT
                        formData={formData}
                        updateFormData={updateFormData}
                    />
                </div>
                <div style={{ display: current === 2 ? 'block' : 'none' }}>
                    <ConfigBox
                        formData={config}
                        updateFormData={updateConfig}
                    />
                </div>
                <div style={{ display: current === 3 ? 'block' : 'none' }}>
                    <ImageConfig
                        formData={formData}
                        updateFormData={updateFormData}
                    />
                </div>
            </>
        )
    }

    const handleSubmit = async (files, configs) => {
        if (!configs.tdId || !configs.voice || configs.hasAction === null || !files.pptFile || !files.avatar) {
            messageApi.open({
                type: 'error',
                content: '请完成所有必填步骤',
            })
            return
        }
        try {
            // 准备表单数据
            const formDatas = new FormData();
            formDatas.append('pptFile', files.pptFile);
            formDatas.append('avatar', files.avatar);
            const configBlob = new Blob(
                [JSON.stringify(config)],
                { type: 'application/json' }
            );
            formDatas.append('config', configBlob, 'config.json')

            // 开始制作
            setIsMaking(true)
            setMakingProgress(0)
            sessionStorage.setItem('isMaking', 'true')
            sessionStorage.setItem('makingProgress', '0')

            // 发送制作请求
            const res = await postVideoConfig(formDatas);
            console.log(res)

            // 开始轮询进度
            startProgressPolling()

        } catch (error) {
            console.error('提交失败:', error)
            messageApi.error('提交失败，请重试')
            setIsMaking(false)
            setMakingProgress(-1)
        }
    }

    return (
       <>
            {contextHolder}


            {!isMaking && <>
                <Steps current={current} items={items} />
                <div className={style.makerContent}>
                    {renderSteps(current)}
                </div>
                <div style={{ marginTop: 24, width: '100%', display: 'flex', justifyContent: 'center' }}>
                    {current > 0 && (
                        <Button style={{ margin: '0 8px' }} onClick={() => prev()}>
                            上一步
                        </Button>
                    )}
                    {current < steps.length - 1 && (
                        <Button type="primary" onClick={() => next()}>
                            下一步
                        </Button>
                    )}
                    {current === steps.length - 1 && (
                        <Button
                            type="primary"
                            onClick={() => handleSubmit(formData, config)}
                            disabled={isMaking} // 制作中禁用按钮
                        >
                            {isMaking ? '制作中...' : '确定制作'}
                        </Button>
                    )}
                </div>
            </>
            }


            {/* 显示进度条 */}
            {isMaking && (
                <div>
                    <ProgressBar progress={makingProgress} />
                </div>
            )}

        </>
    )
}


export default VideoMaker