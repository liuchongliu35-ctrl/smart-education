package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
public class TeachInfoVo {
    @ApiModelProperty(value = "教师id",required = true)
    private Integer uid;
    @ApiModelProperty("账号名")
    private String account;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("姓名")
    private String name;
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty("邮箱")
    private String email;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty("老师电话")
    private String phone;
}
