import { http } from '@/utils/axios'
//根据教师ID获取教案设计历史记录列表
export function getAllDesignList(uid) {
  return http({
    url: `/teachDesign/list/${uid}`,
    method: 'GET',
  })
}
//根据教师ID获取教学设计大纲模板
export function getDesignTemplateList(uid) {
  return http({
    url: `/syllabus/${uid}`,
    method: 'GET',
  })
}
//根据教案设计ID获取教学设计内容
export function getHistoryDesignContent(designId) {
  return http({
    url: `/teachDesign/byId/${designId}`,
    method: 'GET',
  })
}
//文案优化请求
export function postWordModify(data) {
  return http({
    url: `/teachDesign/prompt`,
    method: 'POST',
    data: data
  })
}
//生成图片请求
export function postGeneratePhoto(data) {
  return http({
    url: `/teachDesign/photo`,
    method: 'POST',
    data: data
  })
}
//生成视频链接请求
export function postGenerateVideo(data) {
  return http({
    url: `/teachDesign/video`,
    method: 'POST',
    data: data
  })
}
//获取章节知识点标题
export function getSubjectList(tsId, schoolId) {
  return http({
    url: `/topic/point/choose/11/4`,
    method: 'GET',
  })
}
//AI生成大纲请求
export function postAIGenerate(data) {
  return http({
    url: `/syllabus/fromAI`,
    method: 'POST',
    data: data
  })
}
//保存大纲请求
export function postSaveSyllabus(data) {
  return http({
    url: `/syllabus/save/0`,
    method: 'POST',
    data: data
  })
}
//将模板大纲插入新建教案设计
export function postPushSyllabus(data, tdid) {
  return http({
    url: `/syllabus/save/${tdid}`,
    method: 'POST',
    data: data
  })
}
//根据大纲模板id获取大纲内容
export function getSyllabusContent(sid) {
  return http({
    url: `/syllabus/byId/${sid}`,
    method: 'GET',
  })
}

//新建教学设计
export function postCreateNewDesign(data) {
  return http({
    url: `/teachDesign`,
    method: 'POST',
    data: data
  })
}

//临时保存教学设计内容
export function postEditorCache(data) {
  return http({
    url: `/teachDesign/cache`,
    method: 'POST',
    data: data
  })
}

//获取最近修改的备课板
export function getLastPreparation(uid) {
  return http({
    url: `/teachDesign/last/${uid}`,
    method: 'GET',
  })
}

//获取最近修改的备课板
export function deleteDesignAPI(tdId) {
  return http({
    url: `/teachDesign/remove/${tdId}`,
    method: 'DELETE',
  })
}

//获取知识图谱目录
export function getGraphCatalogue(tsId, schoolId) {
  return http({
    url: `/topic/point/getContents/${tsId}/${schoolId}`,
    method: 'GET',
  })
}

//获取学校知识图谱节点
export function getGraphData(tsId, schoolId) {
  return http({
    url: `/knowledge/graph/4/11`,
    method: 'GET',
  })
}

//获取所有的教学设计与PPT列表
export function getPPTList() {
  return http({
    url: `/teachDesign/getAll`,
    method: 'GET',
  })
}

//根据教学设计id生成ppt(返回ppt链接)
export function getDownloadPPT(tdId) {
  return http({
    url: `/teachDesign/pptApi?tdId=${tdId}`,
    method: 'GET',
  })
}