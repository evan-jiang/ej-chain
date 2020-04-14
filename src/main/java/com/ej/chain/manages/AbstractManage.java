package com.ej.chain.manages;

import com.ej.chain.context.ChainContext;
import com.ej.chain.dto.BaseResponse;
import com.ej.chain.exception.ChainForcedInterruptException;
import com.ej.chain.handlers.Handler;

import java.util.LinkedList;
import java.util.List;


/**
 * 责任链管理类
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 16:20
 */
public abstract class AbstractManage<Request, Data> {

    /**
     * 责任链
     */
    private List<Handler<Request>> chain;

    /**
     * 将Handler注册到责任链中
     *
     * @param handler 业务或校验类Handler
     * @return com.ej.chain.manages.AbstractManage<Request, Data>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:21
     */
    public AbstractManage<Request, Data> register(Handler<Request> handler) {
        if (chain == null) {
            chain = new LinkedList<>();
        }
        chain.add(handler);
        return this;
    }

    /**
     * 执行责任链
     *
     * @param request 请求参数
     * @return com.ej.chain.dto.BaseResponse<Data>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:21
     */
    public BaseResponse<Data> execute(Request request) {
        try {
            for (Handler<Request> handler : chain) {
                if (!handler.checkParams(request)) {
                    break;
                }
                if (handler.idempotent(request)) {
                    continue;
                }
                handler.process(request);
                if (ChainContext.isInterrupted()) {
                    break;
                }
            }
        } catch (ChainForcedInterruptException e) {
            e.printStackTrace();
            ChainContext.injectTips(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
            ChainContext.injectTips(systemErrorCode(), systemErrorMsg());
        } finally {
            BaseResponse baseResponse = ChainContext.baseResponse();
            if (baseResponse.getResponseCode() == null) {
                baseResponse.setResponseCode(successCode());
            }
            if (baseResponse.getResponseMsg() == null) {
                baseResponse.setResponseMsg(successMsg());
            }
            ChainContext.clear();
            return baseResponse;
        }
    }

    /**
     * 每个系统都有自己的系统异常码，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    public abstract String systemErrorCode();

    /**
     * 每个系统都有自己的系统异常描述，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    public abstract String systemErrorMsg();

    /**
     * 每个系统都有自己的操作成功码，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    public abstract String successCode();

    /**
     * 每个系统都有自己的操作成功描述，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    public abstract String successMsg();

}
