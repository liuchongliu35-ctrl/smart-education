package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaHomeworkTrack;
import com.bing.tpa.domain.entity.TpaPreviewTrack;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PreviewCompleteVo {
//    预习任务整体完成情况
    @ApiModelProperty("预习任务整体完成情况")
    private TpaPreviewTrack track;

    @ApiModelProperty("做题详情")
    private List<TrackWithDetails> questionTrackList;

    @ApiModelProperty("预习任务的名字")
    private String previewName;

    @ApiModelProperty("预习任务一级标题")
    private String ptitle;

    @ApiModelProperty("预习任务二级标题")
    private String secondaryTitle;

    @ApiModelProperty("题目总分")
    private Integer score;

    @ApiModelProperty("答对数量")
    private Integer trueNum;

    @ApiModelProperty("答错数量")
    private Integer falseNum;

    @ApiModelProperty("准确率")
    private String trueRate;

    @ApiModelProperty("高频错误知识点")
    private List<Map.Entry<String, Integer>> mistakePoint;

    @ApiModelProperty("预习任务完成用时")
    private String completeTime;

    @ApiModelProperty("学生姓名")
    private String stuName;
}
