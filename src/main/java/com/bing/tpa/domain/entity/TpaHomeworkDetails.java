package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
@TableName("tpa_homework_details")
@ApiModel(value = "TpaHomeworkDetails对象", description = "")
public class TpaHomeworkDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("题目的id")
    @TableId(value = "qid", type = IdType.AUTO)
    private Integer qid;

    @ApiModelProperty("关联的作业id")
    @TableField("hid")
    private Integer hid;

    @ApiModelProperty("关联的作业id")
    @TableField("other_hid")
    private String otherHid;


    @ApiModelProperty("关联的预习任务id预习任务的预习题、补充题都可以放到这张表中")
    @TableField("pt_id")
    private Integer ptId;

    @ApiModelProperty("学生的id，只有该条数据是个性化推荐的就必须和唯一的学生匹配")
    @TableField("sid")
    private Integer sid;


    @ApiModelProperty("表示该题目属于哪一个课程，方便后续自动匹配题库")
    @TableField("subject")
    private String  subject;

    @ApiModelProperty("题目的知识点")
    @TableField("q_title")
    private String qtitle;

    @ApiModelProperty("题目的类型（1:单选, 2:多选, 3:填空, 4:简答）")
    @TableField("q_type")
    private String qtype;

    @ApiModelProperty("不可为null,题目来源1：作业/考试，2：预习任务预习题，3：补充题，-1：表示不属于任何作业，默认为-1，只有和作业或者预习任务进行关联时才进行修改")
    @TableField("q_from")
    private Integer qfrom;

    @ApiModelProperty("题目的内容")
    @TableField("q_content")
    private String qcontent;

    @ApiModelProperty("题目的标准答案")
    @TableField("correct_answer")
    private String correctAnswer;

    @ApiModelProperty("题目解析")
    @TableField("answer_analysis")
    private String answerAnalysis;

    @ApiModelProperty("题目的难度")
    @TableField("q_defficult")
    private String qdefficult;

    @ApiModelProperty("题目默认分值")
    @TableField("default_score")
    private Integer defaultScore;

    @ApiModelProperty("记录该题目被使用的次数(前提是题目是公开的)")
    @TableField("usage_count")
    private Integer usageCount;

    @ApiModelProperty("历史平均准确率")
    @TableField("avg_correct_rate")
    private String avgCorrectRate;

    @ApiModelProperty("该题目被完成的次数")
    @TableField("complete_time")
    private Integer completeTime;

    @ApiModelProperty("被回答正确的次数（同上，不考虑公开的情况）")
    @TableField("correct_time")
    private Integer correctTime;

    @ApiModelProperty("题目创建/生成的时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("题目被修改的时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("审核通过时间")
    @TableField("review_time")
    private LocalDateTime reviewTime;

    @ApiModelProperty("软删除，1没删除，0删除")
    @TableField("is_delete")
    private Integer isDelete;

    @ApiModelProperty("题目的相识度hash值")
    @TableField("q_hash")
    private Integer qhash;

    @ApiModelProperty("如果该题是选择题（单选，多选）,这个字段就记录选项，方便后续渲染")
    @TableField("selections")
    private String selections;

    @ApiModelProperty("如果该题是选择题（单选，多选）,这个字段就记录选项，方便后续渲染")
    @TableField("select_option")
    private String selectOption;

//    选项的数组
    @TableField(exist = false)
    private List<String> options;

    @TableField(exist = false)
    private String stuName;



}
