package com.ej.chain.dto;

import java.io.Serializable;

/**
* 责任链返回封装类
* @author: Evan·Jiang
* @date: 2020/4/14 16:05
*/
public class BaseResponse<Data> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 响应码
     */
    private String responseCode;
    /**
     * 响应描述
     */
    private String responseMsg;
    /**
     * 业务数据
     */
    private Data data;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
