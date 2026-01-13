import { http } from '@/utils/axios'

//视频制作需要的参数配置
export function postVideoConfig(data) {
    return http({
        url: `/video/make`,
        method: 'POST',
        data: data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 获取所有视频列表
export function getVideoList(data) {
    return http({
        url: `/video/videoList`,
        method: 'GET',

    })
}

// 获取视频源文件
export function getVideoFile(videoUrl) {
    return http({
        url: `/video/mp4?videoUrl=${videoUrl}`,
        method: 'GET',
        responseType: 'blob'
    })
}

//获取教学设计列表
export function getDesignList(tid) {
    return http({
        url: `/teachDesign/list/${tid}`,
        method: 'GET',

    })
}

//获取视频进度
export function getVideoMakingProgress() {
    return http({
        url: `/video/status`,
        method: 'GET',
    })
}