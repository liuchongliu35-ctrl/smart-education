package com.bing.tpa.domain.digital;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class LoginRequest {
    private String username;
    private String password; // 如果有密码验证
}
