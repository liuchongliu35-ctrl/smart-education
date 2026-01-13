package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.domain.dto.CheckLessonBean;
import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.domain.entity.TpaSubjectSyllabus;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.DuplicateException;
import com.bing.tpa.exception.LessonException;
import com.bing.tpa.mapper.TpaSubjectMapper;
import com.bing.tpa.mapper.TpaSubjectSyllabusMapper;
import com.bing.tpa.mapper.TpaTeacherMapper;
import com.bing.tpa.service.baseService.TpaSubjectSyllabusService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.utils.TextCompare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaSubjectSyllabusServiceImpl extends ServiceImpl<TpaSubjectSyllabusMapper, TpaSubjectSyllabus> implements TpaSubjectSyllabusService {

    @Autowired
    private TpaSubjectSyllabusMapper syllabusMapper;

//    private TpaSubjectSyllabus newTss=new TpaSubjectSyllabus();

    @Autowired
    private TpaTeacherMapper teacherMapper;

    @Autowired
    private TpaSubjectMapper subjectMapper;


//    TODO 弃用，使用topicPointService中的来代替
//    @Override
//    public PointsVo getSyllabusByTeacherId(Integer tid) {
////       根据学科名从数据库获取该学科的id，在到知识点库中改革局课程id查找所有知识点
////        TpaSubject subject = teacherService.getLessonIdByTid(tid);
//        PointsVo syllabusVo = new PointsVo();
//        TpaSubject subject = getTpaSubject(tid);
////        根据学科的id获取知识点
//        assert subject != null;
//        List<TpaSubjectSyllabus> syllabus = query().eq("ts_id", subject.getTsId()).list();
////       将集合中重复的主题一写到另一个集合中
//        List<String> title=new ArrayList<>();
//        List<String> topTitle=new ArrayList<>();
//        syllabus.forEach(s->{
//            title.add(s.getTopTitle());
//        });
//        title.forEach(t->{
//            if (!topTitle.contains((t))){
//                topTitle.add(t);
//            }
//        });
//        syllabusVo.setSyllabusList(syllabus);
//        syllabusVo.setTopTitle(topTitle);
//        return syllabusVo;
//    }

//    根据老师的id获取该老师教学的科目，我给老师的表中加了一个表示学科id的字段，所以直接根据这个字段进行查找就行了
    private TpaSubject getTpaSubject(Integer tid) {
        TpaTeacher tpaTeacher = teacherMapper.selectById(tid);
        TpaSubject subject = null;
        if (tpaTeacher.getTsId() != null) {
            subject = subjectMapper.selectById(tpaTeacher.getTsId());
        }
        return subject;
    }

//    TODO 由于新的知识点图谱支持在图谱上添加知识点，且自带查重机制(插入局部知识点网时会比那里每一个知识点检查是否在数据库中出现重复)
//    TODO 所以这个检查知识点的代码废弃
//    /**
//     * 检查知识点是否重复，
//     * @param checkLesson
//     * @return
//     */
//    @Override
//    public TpaSubjectSyllabus duplicateCheck(CheckLessonBean checkLesson) throws LessonException, DuplicateException {
//        if(checkLesson.getLessonId()==null) throw new NullPointerException("课程id不能为0");
//        //先根据课程的id找到该课程所有的知识点,这个知识点不会太多，所以可以直接从数据库中查找后再对查找到的内容进行比较
//       QueryWrapper<TpaSubjectSyllabus> queryWrapper=new QueryWrapper<>();
//       queryWrapper.eq("ts_id",checkLesson.getLessonId());
//       List<TpaSubjectSyllabus> tpaSubjectSyllabi = syllabusMapper.selectList(queryWrapper);
//
//       //比较老师定义的两个主题在数据库现有的数据中是否存在
////        检查topTitle
//        if(compareTitle(tpaSubjectSyllabi,1,checkLesson)){
////            主题一相同
//            if(compareTitle(tpaSubjectSyllabi,2,checkLesson)){
//                return newTss;
//            }else {
//                boolean b = addNewOne(checkLesson);
//                if(!b) throw new LessonException("课程添加失败！");
//                return null;
//            }
//        }else {
////            主题一不同，比较主题二
//            if(compareTitle(tpaSubjectSyllabi,2,checkLesson)){
////                单元或章节不同DNA知识点相同这不对，所以应该抛异常
//                throw new DuplicateException("不同章节的知识点不可以重复");
//            }else {
////                如果章节不同且知识点也不同，就直接当做一个新的章节和其中的知识点插入数据库
//                boolean b = addNewOne(checkLesson);
//                if(!b) throw new LessonException("课程添加失败！");
//                return null;
//            }
//        }
//    }
//
//    /*比较主题的算法，一级主题必须在99%以上才算是有相同的，否则就是不同的，所以使用第一种比较算法
//    * 二级主题可以在90%以上就是相同的，所以使用距离算法
//    * */
//    private boolean compareTitle(List<TpaSubjectSyllabus> tpaSubjectSyllabi, int which, CheckLessonBean checkLesson) {
////        等于1表示检查TopTitle,等于2表示检查主题二
//        boolean isSimilar=false;
//        if(which==1){
//            for(TpaSubjectSyllabus tss: tpaSubjectSyllabi){
////            先判断主题一，如果都不相同就表明这是一个新的单元或章节
//                Double cosineSimilarity = TextCompare.getCosineSimilarity(tss.getTopTitle(), checkLesson.getTopTitle());
//                if(cosineSimilarity*100>99) isSimilar=true;//大于99%主题一就是相同的，返回true
//            }
//        }else {
////            否则就是检查secondaryTitle
//                for(TpaSubjectSyllabus tss: tpaSubjectSyllabi){
//                    double v = TextCompare.calculateNormalizedLevenshteinDistance(tss.getSecondaryTitle(), checkLesson.getSecondaryTitle());
//                    if((1-v)*100>90) {
//                        isSimilar=true;
//                        newTss=tss;//将这个重复了的知识点返回，替换用户对那个知识点的命名
//                        break;
//                    }//如果有一条数据的主题二的相似度超过了90%，就表明数据库中有这条知识点
//            }
//        }
//        return isSimilar;
//    }
//
////    添加一条新的知识点到数据库
//    private boolean addNewOne(CheckLessonBean checkLesson){
//        TpaSubjectSyllabus syllabus = new TpaSubjectSyllabus();
//        syllabus.setTsId(checkLesson.getLessonId());
//        syllabus.setTopTitle(checkLesson.getTopTitle());
//        syllabus.setSecondaryTitle(checkLesson.getSecondaryTitle());
//        if (checkLesson.getDefinedTitle()!=null)//将新的也同步给第二个标题
//            syllabus.setSecondaryTitle(checkLesson.getDefinedTitle());
//        else
//            syllabus.setSecondaryTitle(checkLesson.getSecondaryTitle());
//        syllabus.setDefinedTitle(checkLesson.getDefinedTitle());
//        return save(syllabus);
//    }
}
