package com.ej.chain.manages;

import com.ej.chain.context.ChainContext;
import com.ej.chain.dto.BaseResponse;
import com.ej.chain.exception.BizException;
import com.ej.chain.handlers.Handler;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractManage<Request,Data> {

    private List<Handler<Request>> chain;

    public AbstractManage<Request,Data> register(Handler<Request> handler){
        if(chain == null){
            chain = new LinkedList<>();
        }
        chain.add(handler);
        return this;
    }

    public BaseResponse<Data> process(Request request){
        try {
            for (Handler<Request> handler : chain) {
                if(!handler.checkParams(request)){
                    break;
                }
                if(handler.idempotent(request)){
                    continue;
                }
                handler.process(request);
                if(ChainContext.isInterrupted()){
                    break;
                }
            }
        } catch (BizException e){
            e.printStackTrace();
            ChainContext.injectError(e.getErrorCode(),e.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
            ChainContext.injectError(systemErrorCode(),systemErrorMsg());
        } finally {
            BaseResponse baseResponse = ChainContext.baseResponse();
            if (baseResponse.getErrorCode() == null) {
                baseResponse.setErrorCode(successCode());
            }
            if (baseResponse.getErrorMsg() == null) {
                baseResponse.setErrorMsg(successMsg());
            }
            ChainContext.clear();
            return baseResponse;
        }
    }

    public abstract String systemErrorCode();
    public abstract String systemErrorMsg();
    public abstract String successCode();
    public abstract String successMsg();

}
