package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.entity.StudentClass;
import com.bing.tpa.domain.entity.TpaHomeworkSummary;
import com.bing.tpa.domain.entity.TpaStudent;
import com.bing.tpa.mapper.StudentClassMapper;
import com.bing.tpa.mapper.TpaHomeworkSummaryMapper;
import com.bing.tpa.mapper.TpaStudentMapper;
import com.bing.tpa.service.baseService.TpaHomeworkSummaryService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TpaHomeworkSummaryServiceImpl extends ServiceImpl<TpaHomeworkSummaryMapper, TpaHomeworkSummary> implements TpaHomeworkSummaryService {

    @Autowired
    private StudentClassMapper classMapper;

    @Autowired
    private TpaStudentMapper studentMapper;

//    需要将还为开始的名单也加入到summarys 这个里面
    @Override
    public  List<TpaHomeworkSummary> getSummarys(Integer hid, Integer cid) {
//        先根据班级id查询到该班级的所有学生
        List<StudentClass> student = classMapper.getStudent(cid);
//        在将这次作业的完成情况从tpa_homework_summary表中拿出来
        List<TpaHomeworkSummary> summarys = lambdaQuery()
                .eq(TpaHomeworkSummary::getHid, hid)
                .list();
        summarys.forEach(s->{
            TpaStudent student1 = studentMapper.selectById(s.getUid());
            s.setStuCode(student1.getStuNum());
        });
//        将没有完成作业的学生情况也添加到summary中
        for (StudentClass studentClass:student){
            List<TpaHomeworkSummary> oneSummary = summarys.stream()
                    .filter(summary -> {
                        if (summary.getUid()!=null&&summary.getIsComplete()!=0)
                         return summary.getUid().equals(studentClass.getSid());
                        return false;
                    }).collect(Collectors.toList());
            if(oneSummary.size()==0) {
                TpaHomeworkSummary tpaHomeworkSummary = new TpaHomeworkSummary();
                TpaStudent student1 = studentMapper.selectById(studentClass.getSid());
                tpaHomeworkSummary.setStuCode(student1.getStuNum());
                tpaHomeworkSummary.setHid(hid);
                tpaHomeworkSummary.setUid(studentClass.getSid());
                tpaHomeworkSummary.setName(studentClass.getStuName());
                tpaHomeworkSummary.setCompleteTime("--");
                tpaHomeworkSummary.setCompleteQuestion(0);
                tpaHomeworkSummary.setQuestionNum(0);
                tpaHomeworkSummary.setIsComplete(0);
                summarys.add(tpaHomeworkSummary);
            }else  oneSummary.get(0).setName(studentClass.getStuName());
        }
        return summarys;
    }
}
