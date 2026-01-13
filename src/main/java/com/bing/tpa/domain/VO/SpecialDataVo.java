package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class SpecialDataVo {
    @ApiModelProperty("预习任务名字")
    private String previewName;
    @ApiModelProperty("预习任务一级标题")
    private String ptitle;
    @ApiModelProperty("预习任务二级标题")
    private String secondaryTitle;
    @ApiModelProperty("个性化补充资料")
    private String supplement;
    @ApiModelProperty("个性化补充题目")
    private List<TpaHomeworkDetails> specialQuestion;
    @ApiModelProperty("题目数量")
    private Integer qNum;
    @ApiModelProperty("题目总分")
    private Integer score;
}
