package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaHomework;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class HomeworkDTO {
//    作业的状态
    private String hstate;
    private String homeworkName;
    private String hTitle;
    private String secondaryTitle;
    private Integer totalScore;
//    作业对应的题目
    List<TpaHomeworkDetails> details;
}
