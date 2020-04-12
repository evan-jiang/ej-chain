package com.ej.chain.dto;

import java.io.Serializable;

public class BaseResponse<Data> implements Serializable {

    private static final long serialVersionUID = -1L;

    private String errorCode;
    private String errorMsg;
    private Data data;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
