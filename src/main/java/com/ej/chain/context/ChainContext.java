package com.ej.chain.context;

import com.ej.chain.dto.BaseResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 责任链上下文
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 15:19
 */
public class ChainContext {

    /**
     * 线程上下文映射，put需要线程安全，因为key会出现hash碰撞
     */
    private static final Map<Thread, ThreadLocal<Context>> CACHE = new ConcurrentHashMap<>();


    /**
    * 上下文封装类，封装临时变量、中断信号、返回值等等
    * @author: Evan·Jiang
    * @date: 2020/4/14 15:21
    */
    private static class Context {
        private BaseResponse baseResponse;
        private Map<String, Object> temporaryArgs;
        private boolean interrupted = Boolean.FALSE;

    }

    /**
     * 获取ThreadLocal
     * @return java.lang.ThreadLocal<com.ej.chain.context.ChainContext.Context>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:50
     */
    private static ThreadLocal<Context> getThreadLocal() {
        Thread currentThread = Thread.currentThread();
        //对CACHE的非原子操作不需要加锁，因为是key是线程独占
        if (!CACHE.containsKey(currentThread)) {
            ThreadLocal<Context> threadLocal = new ThreadLocal<>();
            CACHE.put(currentThread, threadLocal);
            return threadLocal;
        }
        return CACHE.get(currentThread);
    }

    /**
     * 获取上下文
     * @return com.ej.chain.context.ChainContext.Context
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:50
     */
    private static Context getContext() {
        ThreadLocal<Context> threadLocal = getThreadLocal();
        Context context = threadLocal.get();
        if (context == null) {
            context = new Context();
            threadLocal.set(context);
        }
        return threadLocal.get();
    }

    /**
     * 获取上下文中的返回值
     * @return com.ej.chain.dto.BaseResponse
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:51
     */
    public static BaseResponse baseResponse() {
        Context context = getContext();
        BaseResponse baseResponse = context.baseResponse;
        if (baseResponse == null) {
            context.baseResponse = new BaseResponse();
        }
        return context.baseResponse;
    }

    /**
     * 判断线程是否要终止
     * @return boolean
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:51
     */
    public static boolean isInterrupted() {
        return getContext().interrupted;
    }

    /**
     * 获取上下文临时变量的集合
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:52
     */
    private static Map<String, Object> temporaryArgs() {
        Context context = getContext();
        Map<String, Object> temporaryArgs = context.temporaryArgs;
        if (temporaryArgs == null) {
            context.temporaryArgs = new HashMap<>();
        }
        return context.temporaryArgs;
    }

    /**
     * 保存某个临时变量到上下文中
     * @param key 临时变量名称
     * @param arg 临时变量值
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:52
     */
    public static void injectTemporaryArgs(String key, Object arg) {
        temporaryArgs().put(key, arg);
    }

    /**
     * 从上下文中获取某个临时变量
     * @param key 临时变量名称
     * @return java.lang.Object
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:53
     */
    public static Object extractTemporaryArgs(String key) {
        return temporaryArgs().get(key);
    }

    /**
     * 将提示信息组装返回值设置到上行文中，并设置为中断
     * @param responseCode 提示编码
     * @param responseMsg 提示描述
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:55
     */
    public static void injectTips(String responseCode, String responseMsg) {
        getContext().interrupted = Boolean.TRUE;
        BaseResponse baseResponse = baseResponse();
        baseResponse.setData(null);
        baseResponse.setResponseMsg(responseMsg);
        baseResponse.setResponseCode(responseCode);
    }

    /**
     * 将业务信息组装返回值设置倒数上下文中，并设置为中断
     * @param data 业务信息数据
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:55
     */
    public static void injectData(Object data) {
        getContext().interrupted = Boolean.TRUE;
        BaseResponse baseResponse = baseResponse();
        baseResponse.setData(data);
        baseResponse.setResponseMsg(null);
        baseResponse.setResponseCode(null);
    }

    /**
     * 清空上下文数据
     * @auther: Evan·Jiang
     * @date: 2020/4/14 15:59
     */
    public static void clear() {
        getThreadLocal().remove();
        CACHE.remove(Thread.currentThread());
    }

}
