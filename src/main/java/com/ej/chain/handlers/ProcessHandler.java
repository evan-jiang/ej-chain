package com.ej.chain.handlers;

import com.ej.chain.context.ChainContext;

public interface ProcessHandler<Request> extends Handler<Request> {
    /**
     * 是否重复
     * @param request
     * @return boolean <br/>true:不再执行{@link ProcessHandler#process(Object)}
     * <br/>false:继续执行{@link ProcessHandler#process(Object)}
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:20
     */
    boolean duplicated(Request request);

    /**
     * 业务处理
     * @param request
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:20
     */
    void process(Request request);

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
