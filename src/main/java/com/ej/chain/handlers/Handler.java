package com.ej.chain.handlers;

public interface Handler<Request> {

    /**
     * 参数校验
     * @param request
     * @return boolean
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:19
     */
    boolean checkParams(Request request);

    /**
     * 是否幂等
     * @param request
     * @return boolean
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:20
     */
    boolean idempotent(Request request);

    /**
     * 业务处理
     * @param request
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:20
     */
    void process(Request request);

}
