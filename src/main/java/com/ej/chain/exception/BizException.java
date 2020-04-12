package com.ej.chain.exception;

public class BizException extends RuntimeException {

    static final long serialVersionUID = -1L;
    private String errorCode;
    private String errorMsg;

    public BizException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
