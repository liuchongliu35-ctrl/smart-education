package com.bing.tpa.domain.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bing.tpa.domain.entity.TpaStudent;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StudentListVo extends TpaStudent {

    @ApiModelProperty("该学生在该班级中的排名")
    @TableField("ranking")
    private Integer ranking;

    @ApiModelProperty("AI对学生的综合评价")
    @TableField("evaluation")
    private String evaluation;
}
