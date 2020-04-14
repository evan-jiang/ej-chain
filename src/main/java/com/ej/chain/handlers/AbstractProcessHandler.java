package com.ej.chain.handlers;

public abstract class AbstractProcessHandler<Request> extends AbstractBaseHandler<Request> {

    /**
     * 处理业务类Handler默认是不需要参数校验的，需要可以重载此方法
     * @param request
     * @return boolean
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:19
     */
    public boolean checkParams(Request request) {
        return Boolean.TRUE;
    }

}
