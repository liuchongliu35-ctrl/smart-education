import { http } from '@/utils/axios'

//根据教师ID获取班级列表
export function getClassList(uid) {
  return http({
    url: `/class/classList/${uid}`,
    method: 'GET',
  })
}
//根据班级ID获取班级基本信息
export function getClassInfo(cid) {
  return http({
    url: `/class/classInfo/${cid}`,
    method: 'GET',
  })
}
//根据班级ID获取班级成员
export function getClassMemberList(cid) {
  return http({
    url: `/studentClass/list/${cid}`,
    method: 'GET',
  })
}
//根据班级ID获取预习任务列表
export function getClassPreList(cid) {
  return http({
    url: `/previewTask/taskList/${cid}`,
    method: 'GET',
  })
}


//根据班级ID获取课后作业列表
export function getClassHomeworkList(cid) {
  return http({
    url: `/homework/homeworkList/${cid}`,
    method: 'GET',
  })
}
//根据班级ID和预习任务ID获取班级的一次预习任务完成列表
export function getPreviewDetailTable(ptId, cid) {
  return http({
    url: `/previewTrack/situation/${ptId}/${cid}`,
    method: 'GET',
  })
}
//根据学生ID和预习任务ID获取学生作答详情
export function getStuPreviewDetail(uid, hid) {
  return http({
    url: `/previewTrack/detail/${uid}/${hid}`,
    method: 'GET',
  })
}
//根据班级ID和课后习题ID获取班级的一次课后习题完成列表
export function getHomeworkDetailTable(cid, hid) {
  return http({
    url: `/summary/list/${cid}/${hid}`,
    method: 'GET',
  })
}
//根据班级ID和课后习题ID获取班级的一次课后习题总体情况
export function getHomeworkDetailContent(cid, hid) {
  return http({
    url: `/homework/homeworkSituation/${hid}/${cid}`,
    method: 'GET',
  })
}

//根据学生ID和作业ID获取学生作答详情
export function getStuHomeworkDetail(uid, hid) {
  return http({
    url: `/homeworkTrack/detail/${uid}/${hid}`,
    method: 'GET',
  })
}
