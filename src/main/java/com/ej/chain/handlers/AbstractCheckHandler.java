package com.ej.chain.handlers;

public abstract class AbstractCheckHandler<Request> extends AbstractBaseHandler<Request> {

    /**
     * 校验类Handler不需要单独校验幂等
     * @param request
     * @return boolean
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:17
     */
    public final boolean idempotent(Request request) {
        return false;
    }

    /**
     * 校验类Handler不需要执行process方法
     * @param request
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:17
     */
    public final void process(Request request) {

    }
}
