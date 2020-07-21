package com.ej.chain.manages;

import com.ej.chain.context.ChainContext;
import com.ej.chain.dto.BaseResponse;
import com.ej.chain.exception.ChainForcedInterruptException;
import com.ej.chain.handlers.BaseHandler;
import com.ej.chain.handlers.CheckHandler;
import com.ej.chain.handlers.CompletedHandler;
import com.ej.chain.handlers.ProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


/**
 * 责任链管理类
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 16:20
 */
public abstract class AbstractManage<Request, Data> {

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractManage.class);

    /**
     * 责任链
     */
    private List<BaseHandler<Request>> chain;

    /**
     * 将Handler注册到责任链中
     *
     * @param handler 业务或校验类Handler
     * @return com.ej.chain.manages.AbstractManage<Request, Data>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:21
     */
    public AbstractManage<Request, Data> register(BaseHandler<Request> handler) {
        if (chain == null) {
            chain = new LinkedList<>();
        }
        chain.add(handler);
        return this;
    }

    /**
     * 执行责任链
     *
     * @param request 字符串参数
     * @return com.ej.chain.dto.BaseResponse<Data>
     * @auther: Evan·Jiang
     * @date: 2020/7/21 17:23
     */
    public BaseResponse<Data> execute(String request) {
        try {
            Class<Request> requestClass = getRequestClass();
            Request reqObj = convertRequest(request, requestClass);
            return execute(reqObj);
        } catch (ChainForcedInterruptException e) {
            LOGGER.error("{},errorCode:{},errorMsg:{} -> ", e.getClass().getSimpleName(), e.getErrorCode(), e.getErrorMsg(), e);
            LOGGER.error("{} -> ", e.getClass().getSimpleName(), e);
            BaseResponse baseResponse = new BaseResponse<>();
            baseResponse.setResponseCode(e.getErrorCode());
            baseResponse.setResponseMsg(e.getErrorMsg());
            return baseResponse;
        } catch (Exception e) {
            LOGGER.error("{} -> ", e.getClass().getSimpleName(), e);
            BaseResponse baseResponse = new BaseResponse<>();
            baseResponse.setResponseCode(systemErrorCode());
            baseResponse.setResponseMsg(systemErrorMsg());
            return baseResponse;
        }
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
            for (BaseHandler<Request> handler : chain) {
                if (handler instanceof CheckHandler) {
                    ((CheckHandler) handler).checkParams(request);
                } else if (handler instanceof ProcessHandler) {
                    boolean duplicated = ((ProcessHandler) handler).duplicated(request);
                    if (ChainContext.isInterrupted()) {
                        break;
                    }
                    if (duplicated) {
                        continue;
                    }
                    ((ProcessHandler) handler).process(request);
                } else if (handler instanceof CompletedHandler) {
                    ((CompletedHandler) handler).completed(request);
                }
                if (ChainContext.isInterrupted()) {
                    break;
                }
            }
        } catch (ChainForcedInterruptException e) {
            LOGGER.error("{},errorCode:{},errorMsg:{} -> ", e.getClass().getSimpleName(), e.getErrorCode(), e.getErrorMsg(), e);
            ChainContext.injectTips(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            LOGGER.error("{} -> ", e.getClass().getSimpleName(), e);
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
    protected abstract String systemErrorCode();

    /**
     * 每个系统都有自己的系统异常描述，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    protected abstract String systemErrorMsg();

    /**
     * 每个系统都有自己的操作成功码，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    protected abstract String successCode();

    /**
     * 每个系统都有自己的操作成功描述，各个系统自己定义
     *
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:23
     */
    protected abstract String successMsg();

    /**
     * 当外部参数是String时需要反序列化参数为java对象
     *
     * @param request
     * @param clazz
     * @return Request
     * @auther: Evan·Jiang
     * @date: 2020/7/21 17:25
     */
    protected Request convertRequest(String request, Class<Request> clazz) {
        throw new RuntimeException("需要子类【" + this.getClass().getName() + "】实现convertRequest方法");
    }

    /**
     * 为反序列化对象提供class
     *
     * @return java.lang.Class<Request>
     * @auther: Evan·Jiang
     * @date: 2020/7/21 17:26
     */
    protected Class<Request> getRequestClass() {
        throw new RuntimeException("需要子类【" + this.getClass().getName() + "】实现getRequestClass方法");
    }


}
