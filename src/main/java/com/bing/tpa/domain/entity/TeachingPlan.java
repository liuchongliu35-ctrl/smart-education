package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("teaching_plans")
public class TeachingPlan {
    @TableId(value = "plan_id",type = IdType.AUTO)
    private Integer planId;
    private Integer tsId;
    private Integer schoolId;
    private Integer tssId;
    private Integer planOrder;
}
