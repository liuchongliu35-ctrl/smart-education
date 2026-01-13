package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Getter
@Setter
@TableName("tpa_interaction")
@ApiModel(value = "TpaInteraction对象", description = "")
public class TpaInteraction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "互动id",required = true)
    @TableId(value = "hd_id", type = IdType.AUTO)
    private Integer hdId;

    @ApiModelProperty(value = "关联的教学设计",required = true)
    @TableField("td_id")
    private Integer tdId;

    @ApiModelProperty("互动方式")
    @TableField("method")
    private String method;

    @ApiModelProperty("互动内容")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "学生的回答，主观题就记录学生回答的文字；客观题就记录对错比例格式为：12/2/1，右边是没有回答的人数",required = true)
    @TableField("replay")
    private String replay;

    @ApiModelProperty(value = "学生回答过程中生成有效的观点数",required = true)
    @TableField("viewpoint_num")
    private Integer viewpointNum;

    @ApiModelProperty(value = "参与人数，统计互动积极度",required = true)
    @TableField("participates")
    private Integer participates;

    @ApiModelProperty(value = "回答对的人数",required = true)
    @TableField("correct")
    private Integer correct;

    @ApiModelProperty(value = "学生提问",required = true)
    @TableField("ask_content")
    private String askContent;

    @ApiModelProperty(value = "讨论的时长",required = true)
    @TableField("deadline")
    private LocalDateTime deadline;

    @ApiModelProperty("该题目的状态，1：可参与，0：不可参与")
    @TableField("is_active")
    private Integer isActive;

    @ApiModelProperty("客观题或主观题（1客观，2主观）")
    @TableField("question_type")
    private Integer questionType;
//    create_time
    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("互动题目的答案集合")
    @TableField("answer")
    private String answer;

    @TableField(exist = false)
    private List<TpaHomeworkDetails> details;
    @TableField(exist = false)
    private List<String> answerList;
    @TableField(exist = false)
    private List<String> analysisList;

}
