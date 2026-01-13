package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class HomeworkVo {
//  标志
    private String hstate;
//    表示Ai生成的题目和哪一个作业进行关联
    private String htitle;
    private String homeworkName;
    private String secondaryTitle;
    private Integer totalScore;
    private Integer hid;
//    AI生成的题目详情
    private List<TpaHomeworkDetails> details;
}
