package com.zhang.mypan.exception;

import com.zhang.mypan.enums.CodeEnum;

public class MyPanException extends RuntimeException {
    private CodeEnum codeEnum = CodeEnum.INTERNAL_SERVER_ERROR;

    public MyPanException(String message, CodeEnum codeEnum) {
        super(message);
        this.codeEnum = codeEnum;
    }

    public MyPanException(String message) {
        super(message);
    }

    public MyPanException(CodeEnum codeEnum) {
        super(codeEnum.getErrorMsg());
        this.codeEnum = codeEnum;
    }

    public MyPanException() {
        super();
    }

    public CodeEnum getCodeEnum() {
        return codeEnum;
    }
}
