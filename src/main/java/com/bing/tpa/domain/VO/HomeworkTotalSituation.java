package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HomeworkTotalSituation {
    @ApiModelProperty("作业名称")
    private String hName;
    @ApiModelProperty("作业一级标题")
    private String htitle;
    @ApiModelProperty("作业二级标题")
    private String secondaryTitle;

    @ApiModelProperty("总人数")
    private Integer totalPerson;
    @ApiModelProperty("完成人数")
    private Integer complete;
    @ApiModelProperty("未完成人数")
    private Integer uncomplete;
    @ApiModelProperty("完成率")
    private Double completeRate;
    @ApiModelProperty("作业平均分")
    private Double avgScore;
    @ApiModelProperty("高频错误知识点")
    private List<Map.Entry<String, Integer>>  mistakePoint;//每一个知识点错误的人数，人数最多的就是错的最多的知识点
    @ApiModelProperty("优秀作业个数")
    private Integer good;
    @ApiModelProperty("中等作业个数")
    private Integer middle;
    @ApiModelProperty("较差作业个数")
    private Integer poor;
    @ApiModelProperty("作业优秀率")
    private Double goodRate;
    @ApiModelProperty("及格率")
    private Double passingRate;
    @ApiModelProperty("不及格率")
    private Double unPassingRate;
}
