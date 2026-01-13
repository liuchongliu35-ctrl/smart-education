package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

//该类用于根据学生id，作业id，题目id以及答案来更新redis中储存的题目作答数据
@Data
public class TrackUpdateVo {
//    学生id
    @NotNull
    @ApiModelProperty(value = "用户id",required = true)
    private Integer id;
//    作业id或者预习任务id
    @NotNull
    @ApiModelProperty(value = "作业或预习任务id",required = true)
    private Integer tid;
//    要更新的这个题的id
    @NotNull
    @ApiModelProperty(value = "正在做的题目的id",required = true)
    private Integer qid;
//    下一个题目的id，为了设置下一个题目开始的时间
    @NotNull
    @ApiModelProperty(value = "学生即将做的下一道题的id",required = true)
    private Integer nextQid;
//    这个题目的答案
    @NotNull
    @ApiModelProperty(value = "学生正在做的题目的回答",required = true)
    private String answer;
}
