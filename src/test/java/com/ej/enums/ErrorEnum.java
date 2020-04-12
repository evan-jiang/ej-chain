package com.ej.enums;

public enum  ErrorEnum {
    SYSTEM_ERROR("999999","系统异常"),
    SUCCESS("000000","操作成功"),
    PARAMS_ERROR("100001","参数错误-%s"),
    ;

    private String errorCode;
    private String errorMsg;

    ErrorEnum(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String buildErrorMsg(Object ... objects){
        return String.format(getErrorMsg(),objects);
    }
}
