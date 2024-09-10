package com.zhang.mypan.enums;

public enum ShareVaildTypeEnums {
    DAY_1(0, 1, "1天"),
    DAY_7(1, 7, "7天"),
    DAY_30(2, 30, "30天"),
    FOREVER(3, -1, "永久");

    private Integer type;
    private Integer days;
    private String desc;

    ShareVaildTypeEnums(Integer type, Integer days, String desc) {
        this.type = type;
        this.days = days;
        this.desc = desc;
    }

    public static ShareVaildTypeEnums getByCode(Integer validType) {
        for (ShareVaildTypeEnums shareVaildTypeEnums : ShareVaildTypeEnums.values()) {
            if (shareVaildTypeEnums.getType().equals(validType)) {
                return shareVaildTypeEnums;
            }
        }
        return null;
    }


    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
