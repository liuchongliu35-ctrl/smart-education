package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school")
public class School {
    @TableId(value = "school_id",type = IdType.AUTO)
    private Integer schoolId;
    private String schoolName;
    private String schoolShortName;
    private String address;
    private String contact;
    private String contactPhone;
    private LocalDateTime createTime;
    private Integer isActive;
}
