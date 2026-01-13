package com.bing.tpa.common;

public enum ResultCodeEnum {
    SUCCESS("0","success"),
    FAIL("001","操作失败，请重新操作"),
    NOAUTH("1001", "认证失败请重新登录"),
    SAVE_FAIL("505","插入操作失败"),
    UPDATE_FAIL("506", "更新失败"),
    PARAM_ERROR("507","传参错误" ),
    NOACCESS("1002","权限不足");
    private String code;
    private String message;

    ResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
