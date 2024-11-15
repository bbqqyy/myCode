package com.bqy.user_center.utils;

/**
 * 错误码
 */
public enum ErrorCode {

    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NO_AUTH(40101,"无权限",""),
    NOT_LOGIN(40100,"未登录",""),
    SYSTEM_ERROR(50000,"系统内部异常",""),
    OPERATION_ERROR(50001, "操作失败", "操作失败"),
    INVALID_PASSWORD_ERROR(40102, "无效密码", "");

    private final int code;
    /**
     * 状态码信息
     */
    private final String msg;
    /**
     * 状态码信息
     */
    private final String description;

    ErrorCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}
