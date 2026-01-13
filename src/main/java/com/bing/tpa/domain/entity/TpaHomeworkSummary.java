package com.bing.tpa.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.xml.transform.sax.SAXResult;

@Getter
@Setter
@TableName("tpa_homework_summary")
@ApiModel(value = "TpaHomeworkDetails对象", description = "")
public class TpaHomeworkSummary {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录学生完成整个作业的情况")
    @TableId(value = "tht_id", type = IdType.AUTO)
    private Integer thtId;


    @ApiModelProperty("关联的作业id")
    @TableField("hid")
    private Integer hid;

//    老师的id后面需要加00+id,比如老师的id为12，则这里记录的就是120012
//    学生的id是多少就是多少
    @ApiModelProperty("关联的用户id")
    @TableField("uid")
    private Integer uid;


    @ApiModelProperty("用户的姓名")
    @TableField("name")
    private String name;

    @ApiModelProperty("作业完成用时")
    @TableField("complete_time")
    private String completeTime;


    @ApiModelProperty("作业得分")
    @TableField("score")
    private Integer score;

    @ApiModelProperty("完成的题目数量")
    @TableField("complete_question")
    private Integer completeQuestion;


    @ApiModelProperty("作业总的题目量")
    @TableField("question_num")
    private Integer questionNum;

    @ApiModelProperty("错题数")
    @TableField("mistake")
    private String mistake;

    @ApiModelProperty("错题数")
    @TableField("is_complete")
    private Integer isComplete;

    @ApiModelProperty("AI生成作业报告")
    @TableField("report")
    private String report;

    @TableField(exist = false)
    private String stuCode;



}
