package com.ej.chain.exception;


/**
 * 责任链强制中断异常，业务可以继承使用，以抛出此异常类其子类的方式中断责任链
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 16:13
 */
public class ChainForcedInterruptException extends RuntimeException {

    static final long serialVersionUID = -1L;
    /**
     * 异常错误码
     */
    private String errorCode;
    /**
     * 异常错误描述
     */
    private String errorMsg;

    public ChainForcedInterruptException(String errorCode, String errorMsg) {
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
