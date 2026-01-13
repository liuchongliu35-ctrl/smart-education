import React, { useState, useEffect } from 'react'
import { Progress, Button } from 'antd'
import { grid } from 'ldrs'
grid.register()



const ProgressBar = ({ progress = -1 }) => {

    // 双色配置：前半部分为蓝色，后半部分为绿色
    const twoColors = {
        '0%': '#64e9ef',
        '100%': '#103ce7',
    }



    // 获取状态文本
    const getStatusText = () => {
        if (progress === -1) return '等待开始'
        if (progress === 0) return '初始化中...'
        if (progress < 30) return '正在切分PPT...'
        if (progress < 60) return '处理视频内容...'
        if (progress < 90) return '视频合成阶段...'
        if (progress < 100) return '最终渲染...'
        if (progress === 101) return '视频制作完成！'
        return '处理中...'
    }

    return (
        <div style={{ maxWidth: 800, margin: '100px auto', padding: 20 }}>

            {progress >= 0 && progress <= 100 && (
                <div style={{ marginBottom: 30, textAlign: 'center' }}>
                    <div style={{ width: '100%', marginBottom: 40 }}>
                        <l-grid size="70" speed="2.5" color="#ff7c1d"></l-grid>
                    </div>
                    <Progress
                        percent={progress}
                        strokeColor={twoColors}
                        strokeWidth={18}
                        strokeLinecap="round"
                        status={progress === 101 ? 'success' : 'active'}
                        format={() => (
                            <div style={{
                                fontWeight: 'bold',
                                color: progress < 50 ? '#1890ff' : '#103ce7',
                                fontSize: 18
                            }}>
                                {progress === 101 ? '100%' : `${progress}%`}
                            </div>
                        )}
                    />

                    <div style={{
                        marginTop: 20,
                        fontSize: 16,
                        fontWeight: 'bold',
                        color: progress === 101 ? '#103ce7' : '#333',
                        minHeight: 24
                    }}>
                        {getStatusText()}
                    </div>
                </div>
            )}

            {progress === 101 && (
                <div style={{ textAlign: 'center', marginTop: 30 }}>
                    <Button
                        type="primary"
                        size="large"
                        onClick={() => window.location.reload()} // 刷新页面或跳转到视频列表
                    >
                        查看制作的视频
                    </Button>
                </div>
            )}


            <div style={{ marginTop: 30, textAlign: 'center', color: '#666' }}>
                <h3>视频制作说明</h3>
                <p>制作时间与PPT内容丰富度和页数有关，请耐心等待</p>
                <p>视频数字人形象与提供的人像素材会一定的出入，实际情况请以生成的视频为主</p>
            </div>
        </div>
    );
};

export default ProgressBar