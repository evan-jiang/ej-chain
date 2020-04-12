package com.ej.chain.handlers;

public interface Handler<Request> {

    boolean checkParams(Request request);

    boolean idempotent(Request request);

    void process(Request request);

}
