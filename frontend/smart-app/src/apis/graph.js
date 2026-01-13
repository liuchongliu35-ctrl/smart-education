import { http } from '@/utils/axios'

//根据知识点匹配教学设计
export function getPointDesign(title) {
    return http({
        url: `/teachDesign/matchDesign?title=${title}`,
        method: 'GET',
    })
}
//根据知识点匹配PPT
export function getPointPPT(title) {
    return http({
        url: `/teachDesign/getPpt?title=${title}`,
        method: 'GET',
    })
}
//根据PPT路径获取PPT文件
export function getPPTFile(pptUrl) {
    return http({
        url: `/teachDesign/ppt?pptUrl=${pptUrl}`,
        method: 'GET',
        responseType: 'blob',
        headers: {
            'Accept': 'application/octet-stream'
        }
    })
}

//根据知识点匹配作业
export function getPointHomework(title, uid) {
    return http({
        url: `/homework/point-homework?title=${title}&uid=${uid}`,
        method: 'GET',
    })
}

//根据知识点匹配预习任务
export function getPointPreview(title, tid) {
    return http({
        url: `/previewTask/point-preview?title=${title}&tid=${tid}`,
        method: 'GET',
    })
}

//根据知识点获取相关的视频列表
export function getPointVideo(title) {
    return http({
        url: `/video/video-point?title=${title}`,
        method: 'GET',
    })
}