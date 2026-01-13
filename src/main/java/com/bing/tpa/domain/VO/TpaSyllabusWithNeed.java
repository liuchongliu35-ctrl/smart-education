package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaSyllabus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TpaSyllabusWithNeed extends TpaSyllabus {
//   用户对应AI生成的大纲的额外需求
@ApiModelProperty(value = "用户对Ai生成大纲的额外需求",required = true)
    private String extraRequirements;
//    额外限制条件
@ApiModelProperty(value = "额外限制条件",required = true)
    private String extraRestrictions;

//    教学大纲的知识点限制
    @ApiModelProperty(value = "教学设计的一级主题（主题可以限制章节/单元）",required = true)
    private String designTitle;

    @ApiModelProperty(value = "教学设计的二级标题（具体的知识点，该单元里的某一个小节）",required = true)
    private String secondaryTitle;
}
