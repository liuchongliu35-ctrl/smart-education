import { http } from '@/utils/axios'
//获取历史预习任务列表
export function getPreList(uid) {
  return http({
    url: `/previewTask/taskByTid/${uid}`,
    method: 'GET',
  })
}
//根据预习任务ID获取预习任务内容
export function getPreContent(ptId) {
  return http({
    url: `/previewTask/taskDetailById/${ptId}/0`,
    method: 'GET',
  })
}
//获取历史课后习题列表
export function getHomeworkList(uid) {
  return http({
    url: `/homework/homeworkByTid/${uid}`,
    method: 'GET',
  })
}
//根据作业任务ID获取预习任务内容
export function getHomeworkContent(hid) {
  return http({
    url: `/homeworkDetails/list/${hid}`,
    method: 'GET',
  })
}

//获取章节知识点标题
export function getSubjectList(tsId, schoolId) {
  return http({
    url: `/topic/point/choose/${tsId}/${schoolId}`,
    method: 'GET',
  })
}
//创建预习任务请求
export function postCreatePreview(data, uid) {
  return http({
    url: `/previewTask/generate/${uid}`,
    method: 'POST',
    data: data
  })
}
//保存新建的预习任务
export function postSavePreview(data) {
  return http({
    url: `/previewTask/save`,
    method: 'POST',
    data: data
  })
}
//创建课后习题请求
export function postCreateHomework(data, uid) {
  return http({
    url: `/homework/generate/${uid}`,
    method: 'POST',
    data: data
  })
}
//保存新建的课后作业
export function postSaveHomework(data, hid) {
  return http({
    url: `/homeworkDetails/add/${hid}`,
    method: 'POST',
    data: data
  })
}
//发布预习任务请求
export function putPublishPre(data) {
  return http({
    url: `/previewTask/release`,
    method: 'PUT',
    data: data
  })
}
//发布课后习题请求 
export function putPublishWork(data) {
  return http({
    url: `/homework/release`,
    method: 'PUT',
    data: data
  })
}