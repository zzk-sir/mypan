package com.zhang.mypan.enums;

//用于兼容windows和linux
public enum OSEnums {
    WINDOWS(0, "windows操作系统"),
    LINUX(1, "linux操作系统"),
    MAC(2, "mac操作系统"),
    OTHER(3, "其他");
    private Integer oSCode;
    private String oSName;

    OSEnums(Integer oSCode, String oSsName) {
        this.oSCode = oSCode;
        this.oSName = oSsName;
    }

    public Integer getoSCode() {
        return oSCode;
    }

    public String getoSName() {
        return oSName;
    }

    @Override
    public String toString() {
        return oSName;
    }
}
