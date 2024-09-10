package com.zhang.mypan.dto;

import com.zhang.mypan.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Boolean success;
    private String errorMsg;
    private Object data;
    private Integer code;

    public static Result ok() {
        return new Result(true, null, "ok", 200);
    }

    public static Result ok(Object data) {
        return new Result(true, null, data, 200);
    }

    public static Result fail(String errorMsg, CodeEnum code) {
        return new Result(false, errorMsg, null, code.getErrorCode());
    }

    public static Result fail(CodeEnum code) {
        return new Result(false, code.getErrorMsg(), null, code.getErrorCode());
    }
}
