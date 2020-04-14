package com.ej.chain.handlers;

import com.ej.chain.context.ChainContext;

public abstract class AbstractBaseHandler<Request> implements Handler<Request> {

    /**
     * 将提示信息组装返回值设置到上行文中，并设置为中断
     *
     * @param responseCode
     * @param responseMsg
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:15
     */
    protected void injectTips(String responseCode, String responseMsg) {
        ChainContext.injectTips(responseCode, responseMsg);
    }

    /**
     * 将业务信息组装返回值设置倒数上下文中，并设置为中断
     *
     * @param data 业务信息数据
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:55
     */
    protected void injectData(Object data) {
        ChainContext.injectData(data);
    }

}
