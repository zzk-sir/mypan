package com.zhang.mypan.enums;


public enum FileCategoryEnums {
    VIDEO(1, "video", "视频"),
    MISIC(2, "music", "音频"),
    IMAGE(3, "image", "图片"),
    DOC(4, "doc", "文档"),
    OTHERS(5, "others", "其他");


    private final Integer category;
    private final String code;
    private final String desc;

    FileCategoryEnums(Integer category, String code, String desc) {
        this.category = category;
        this.code = code;
        this.desc = desc;
    }

    public static FileCategoryEnums getByCode(String code) {
        for (FileCategoryEnums fileCategoryEnums : FileCategoryEnums.values()) {
            if (fileCategoryEnums.getCode().equals(code)) {
                return fileCategoryEnums;
            }
        }
        return null;
    }

    public Integer getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
