package com.ej.chain.handlers;

public abstract class AbstractProcessHandler<Request> extends AbstractBaseHandler<Request> {
    @Override
    public boolean checkParams(Request request) {
        return Boolean.TRUE;
    }

}
