package com.bing.tpa.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {

    @NotBlank(message = "账号名不能为空")
    private String username;

    
    @NotBlank(message = "密码不能为空")
    private String password;
}
