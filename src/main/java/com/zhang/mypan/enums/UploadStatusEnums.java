package com.zhang.mypan.enums;

public enum UploadStatusEnums {
    UPLOAD_SECONDS("upload_seconds", "秒传"),
    UPLOADING("uploading", "上传中"),
    UPLOAD_FINISH("upload_finish", "上传完成"),
    UPLOAD_FAIL("upload_fail", "上传失败");

    private final String code;
    private final String desc;

    UploadStatusEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
