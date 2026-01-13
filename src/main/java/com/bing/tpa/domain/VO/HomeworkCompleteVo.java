package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaHomeworkSummary;
import com.bing.tpa.domain.entity.TpaHomeworkTrack;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HomeworkCompleteVo {
    @ApiModelProperty("做题详情")
    private List<TrackWithDetails> trackList;
    @ApiModelProperty("作业完成整体情况")
    private TpaHomeworkSummary summary;
    @ApiModelProperty("作业名字")
    private String hName;
    @ApiModelProperty("作业一级标题")
    private String htitle;
    @ApiModelProperty("作业二级标题")
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
}
