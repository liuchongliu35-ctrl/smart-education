package com.bing.tpa.service.baseImpl;

import com.bing.tpa.domain.dto.SubjectDTO;
import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.exception.DatabaseException;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.TpaSubjectMapper;
import com.bing.tpa.mapper.TpaTeacherMapper;
import com.bing.tpa.service.baseService.TpaSubjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.utils.TypeCheck;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class TpaSubjectServiceImpl extends ServiceImpl<TpaSubjectMapper, TpaSubject> implements TpaSubjectService {

//    SubjectStage不可由用户自定义，是硬性的要求：小学，初中，高中，大学，小学初中的册数只能是上下册，高中大学随意
    @Autowired
    private TpaTeacherMapper teacherMapper;
    @Autowired
    private TpaSubjectMapper subjectMapper;

//检查添加的学科是否重复
    @Override
    public Integer check(TpaSubject subject, Integer uid) {
//        检查subject中的课程信息不要包含一些奇怪的符号
        boolean typeCheck = TypeCheck.isAllChinese(subject.getSubjectName()) && TypeCheck.isAllChinese(subject.getSubjectStage()) && TypeCheck.isAllChinese(subject.getVolume());
        if (!typeCheck) return -2;
        TpaSubject matchSubject = lambdaQuery(subject)
                .eq(TpaSubject::getSubjectName, subject.getSubjectName())
                .eq(TpaSubject::getSubjectStage, subject.getSubjectStage())
                .eq(TpaSubject::getGrade, subject.getGrade())
                .eq(TpaSubject::getVolume, subject.getVolume())
                .one();
//        没找到符合条件的数据
        if(matchSubject==null){
//            将新的课程加入学科表，如果没找到，那么就一定会向学科数据库中加入一条新的学科，后续根据老师的Id查找也一定可以找到
            boolean save = save(subject);
//            将新添加的数据的id返回,并将这个新的学科id给这个老师
            if(save&&subject.getTsId()!=null) {
                System.out.println(subject.getTsId());
                System.out.println(uid);
                teacherMapper.addSubjectId(subject.getTsId(),uid);
                return subject.getTsId();}
        }
//        找到了，返回-1，表示不用进行下一步操作了,
//        同时将这个学科的id给该老师
        assert matchSubject != null;
        teacherMapper.addSubjectId(matchSubject.getTsId(),uid);
//        如果这里就找到了，那么后面只要根据老师的id查找老师教授的课程信息，就一定可以找到一条课程信息的id
        return -1;
    }

    @Override
    public Integer getIdByTeacherId(Integer tid) {

        return null;
    }


    @Override
    public TpaSubject createSubject(SubjectDTO subjectDTO, Integer schoolId) throws DatabaseException {

        TpaSubject subject = null;
        try {
            subject = new TpaSubject();
            BeanUtils.copyProperties(subjectDTO, subject);
            subject.setIsOpen(subjectDTO.getIsOpen());
            System.out.println("课程开放状态为"+subjectDTO.getIsOpen());
            subject.setSchoolId(schoolId);//课程与学校绑定！！！
            boolean save = save(subject);
            if (!save){
                return null;
            }
        } catch (BeansException e) {
            throw new DatabaseException("课程创建失败");
        }
        return subject;
    }

    @Override
    public List<TpaSubject> getSubjectsBySchoolId(Integer schoolId) {
        return subjectMapper.findBySchoolId(schoolId);
    }

    @Override
    public TpaSubject getSubjectById(Integer subjectId) {
        return subjectMapper.selectById(subjectId);
    }



}
