package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaPreviewTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
//@EqualsAndHashCode(callSuper = true)
public class PreviewTaskVo  {
//    预习任务的状态
    private String pstate;
//    预习任务的id
@ApiModelProperty(value = "预习任务的id",required = true)
    private Integer ptId;
@ApiModelProperty(value = "预习任务的名称")
    private String previewName;

@ApiModelProperty("预习题的分数")
    private Integer totalScore;
@ApiModelProperty("预习任务一级标题")
    private String ptitle;
@ApiModelProperty("预习任务二级标题")
    private String secondaryTitle;
//    预习题
@ApiModelProperty(value = "预习题",required = true)
    private List<TpaHomeworkDetails> taskList;
//    预习资料
@ApiModelProperty(value = "预习资料",required = true)
    private TaskResourceVo previewText;
}
