package com.bing.tpa.Thread;


import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.LessonException;
import com.bing.tpa.mapper.TpaTeacherMapper;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.service.baseService.TpaSubjectService;
import com.bing.tpa.service.baseService.TpaSubjectSyllabusService;
import com.bing.tpa.utils.TextCompare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class SubjectUpdateThread {

    @Autowired
    private TpaSubjectService subjectService;

    @Autowired
    private TpaSubjectSyllabusService subjectSyllabusService;

    @Autowired
    private TpaTeacherMapper teacherMapper;

    @Autowired
    private ChatWithModel chatWithModel;

    private static final String botId="7473462018583134247";

//  TODO 弃用这个检查课程知识点的代码

//    public Future<Object> checkAndUpdate(TpaSubject subject, Integer uid) {
//        try {
//            // 检查学科库中是否有该教师教授的课程,将老师的id传过去，后续为老师添加课程的id
//            Integer check = subjectService.check(subject,uid);
//            if (check == null) {
//                throw new NullPointerException("获取新课程id失败！");
//            }
//            if (check != -1) {
//                // 使用check这个新课程的id将该课程的所有知识点生成，并插入到知识点表中
//                String answer = null;
//                try {
//                    answer = getString(subject);
//                    // 处理返回的内容
//                    if (Objects.equals(answer, "NULL")) {
//                        answer = getString(subject);
//                        if (Objects.equals(answer, "NULL")) {
//                            // 如果找不到相关的知识点，就表明老师在之前填写信息时选择的课程信息不合规
//                            List<TpaSubject> subjects = subjectService.list();
//                            // 计算每门课程的相似度
//                            Map<TpaSubject, Double> similarityMap = subjects.stream()
//                                    .collect(Collectors.toMap(
//                                            subject1 -> subject,
//                                            subject1 -> TextCompare.calculateSimilarity(subject, subject1),
//                                            (existing, replacement) -> existing
//                                    ));
//
//                            // 找到相似度最高的课程
//                            TpaSubject tpaSubject = similarityMap.entrySet().stream()
//                                    .max(Map.Entry.comparingByValue())
//                                    .map(Map.Entry::getKey)
//                                    .orElse(null);
//
//                            if (tpaSubject == null) {
//                                // 如果数据库中也没有，则抛异常,表明盖老师选的这课程有问题，所以将新加入的老师的数据删除，让老师重新进行信息填写
//                                teacherMapper.deleteById(uid);
//                                throw new LessonException("课程名或课程模块有误");
//                            }
//
//                            TpaTeacher tpaTeacher = new TpaTeacher();
//                            tpaTeacher.setTeachLesson(tpaSubject.getSubjectName());
//                            tpaTeacher.setTeachStage(tpaSubject.getSubjectStage());
//                            tpaTeacher.setStageNum(tpaSubject.getGrade());
//                            tpaTeacher.setUid(uid);
//                            tpaTeacher.setTsId(tpaSubject.getTsId());
//                            // 这里老师的课程信息也来自于学科表，因此后续根据老师的id查找课程信息，再到学科表中匹配也一定可以匹配到数据
//                            teacherMapper.updateById(tpaTeacher);
//                        }
//                    }
//                } catch (LessonException e) {
//                    e.printStackTrace();
//                    // 返回包含异常信息的结果
//                    return new AsyncResult<>(e.getMessage());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    // 返回包含异常信息的结果
//                    return new AsyncResult<>("服务器内部错误");
//                }
//
//                List<TpaSubjectSyllabus> tpaSubjectSyllabi = stringUtils.parseSyllabus(answer, check);
//                // 将每一个TpaSubjectSyllabus对象插入数据库
//                boolean b = subjectSyllabusService.saveBatch(tpaSubjectSyllabi);
//                if (!b) {
//                    throw new RuntimeException("知识点库更新失败！");
//                }
//            }
////            课程信息的数据类型错误，不能进行查询是否存在数据库中，或者进行其他操作
//            if (check==-2){
//                return new AsyncResult<>("数据类型错误");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            // 返回包含异常信息的结果
//            return new AsyncResult<>(e.getMessage());
//        }
//
//        return new AsyncResult<>("操作成功");
//    }

//    TODO 弃用
//    private String getString(TpaSubject subject) throws Exception {
//        String answer = null;
//        try {
//            answer = chatWithModel.chatClient(
//                    subject.getSubjectStage() + subject.getGrade() + "年级" +
//                            subject.getSubjectName() + subject.getVolume() + "人教版知识点梳理" +
//                            "\n格式要求：1、按照章节1主题+章节1内容，章节2主题+章节2内容.....的格式返回\n2、一定要将生成的文字中所有markdown符号都去掉",
//                    "1",botId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NullPointerException("网络出问题，智能体生成知识点失败");
//        }
//        return answer;
//    }
}
