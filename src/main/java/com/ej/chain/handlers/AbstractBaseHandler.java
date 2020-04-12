package com.ej.chain.handlers;

import com.ej.chain.context.ChainContext;
import com.ej.chain.dto.BaseResponse;

public abstract class AbstractBaseHandler<Request> implements Handler<Request> {

    protected void injectError(String errorCode,String errorMsg){
        ChainContext.injectError(errorCode,errorMsg);
    }

    protected void injectData(Object data){
        ChainContext.injectData(data);
    }

}
