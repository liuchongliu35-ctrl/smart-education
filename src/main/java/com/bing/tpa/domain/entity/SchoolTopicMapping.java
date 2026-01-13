package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school_topic_mapping")
public class SchoolTopicMapping {
    @TableId(value = "mapping_id",type = IdType.AUTO)
    private Integer mappingId;
    private Integer schoolId;
    private Integer templateId;
    private Integer tssId;
    private Integer isUsed;
    private Integer isCustomized;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
