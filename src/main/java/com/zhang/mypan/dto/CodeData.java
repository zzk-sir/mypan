package com.zhang.mypan.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CodeData {
    private String base64;
    private String key;

    public CodeData(String base64, String key) {
        this.base64 = "data:image/jpeg;base64," + base64;
        this.key = key;
    }

    public static CodeData code(String base64) {
        return new CodeData(base64, UUID.randomUUID().toString());
    }
}
