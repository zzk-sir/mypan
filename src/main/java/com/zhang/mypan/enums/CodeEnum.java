package com.zhang.mypan.enums;

public enum CodeEnum {
    // 数据操作错误定义
    SUCCESS(200, "nice"),
    NO_PERMISSION(403, "你没得权限"),
    NO_AUTH(401, "你能不能先登录一下"),
    NOT_FOUND(404, "未找到该资源!"),
    INTERNAL_SERVER_ERROR(500, "服务器跑路了"),
    BAD_REQUEST(400, "错误的请求"),
    CONFILCT(409, "与原有资源发生冲突"),
    LOGIN_TIMEOUT(901, "登录超时,请重新登录"),
    SPACE_OVERFLOW(904, "网盘空间不足，请扩容"),
    FILE_DOWNLOAD_ERROR(600, "下载文件失败"),
    ISEXPIRED(902, "资源不存在或已失效"),
    CHECKFAIL(903, "验证失败"),
    USERNOTUSE(905, "用户不可用"),
    FILE_TIMEOUT(906, "文件由于时间太长已丢失，请重新上传");

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    CodeEnum(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
