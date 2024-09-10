package com.zhang.mypan.enums;

public enum UserStatus {
    DEL(1, "删除"),
    NORMAL(0, "正常");
    private Integer status;
    private String desc;

    UserStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
