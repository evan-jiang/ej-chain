package com.ej.chain.handlers;

import com.ej.chain.context.ChainContext;

public interface CompletedHandler<Request> extends Handler<Request> {
    /**
     * 处理完成之后的方法
     * @param request
     * @auther: Evan·Jiang
     * @date: 2020/4/29 17:05
     */
    void completed(Request request);

    /**
     * 将业务信息组装返回值设置倒数上下文中，并设置为中断
     *
     * @param data 业务信息数据
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:55
     */
    default void injectData(Object data) {
        ChainContext.injectData(data);
    }
}
