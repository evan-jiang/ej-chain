package com.ej.chain.handlers;

import com.ej.chain.context.ChainContext;

public interface CheckHandler<Request> extends BaseHandler<Request> {

    /**
     * 参数校验
     * @param request
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:19
     */
    void checkParams(Request request);

    /**
     * 将提示信息组装返回值设置到上行文中，并设置为中断
     *
     * @param responseCode
     * @param responseMsg
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:15
     */
    default void injectTips(String responseCode, String responseMsg) {
        ChainContext.injectTips(responseCode, responseMsg);
    }
}
