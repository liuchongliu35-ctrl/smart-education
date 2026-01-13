package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PreviewTextVo {
//    预习任务id
@ApiModelProperty(value = "预习任务的id",required = true)
    private Integer ptId;
//    用户id
@ApiModelProperty(value = "用户的id",required = true)
    private Integer uid;
//    附加题答案，使用数组来收集附加题的答题情况
@ApiModelProperty(value = "预习资料附加题答案",required = true)
    private List<String> answer;
//    对预习资料不断地地方,使用一个数组来收集不懂的地方
@ApiModelProperty(value = "预习资料不懂问题收集",required = true)
    private List<String> dataInquiry;
}
