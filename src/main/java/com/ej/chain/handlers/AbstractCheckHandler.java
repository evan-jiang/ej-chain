package com.ej.chain.handlers;

public abstract class AbstractCheckHandler<Request> extends AbstractBaseHandler<Request> {

    @Override
    public boolean idempotent(Request request) {
        return false;
    }

    @Override
    public void process(Request request) {

    }
}
