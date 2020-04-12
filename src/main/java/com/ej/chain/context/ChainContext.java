package com.ej.chain.context;

import com.ej.chain.dto.BaseResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChainContext {

    private static final Map<Thread,ThreadLocal<Context>> CACHE = new ConcurrentHashMap<>();

    private static class Context {
        private BaseResponse baseResponse;
        private Map<String,Object> temporaryArgs;
        private boolean interrupted = Boolean.FALSE;

    }

    private static ThreadLocal<Context> getThreadLocal(){
        Thread currentThread = Thread.currentThread();
        if(!CACHE.containsKey(currentThread)){
            ThreadLocal<Context> threadLocal = new ThreadLocal<>();
            CACHE.put(currentThread,threadLocal);
            return threadLocal;
        }
        return CACHE.get(currentThread);
    }

    private static Context getContext(){
        ThreadLocal<Context> threadLocal = getThreadLocal();
        Context context = threadLocal.get();
        if(context == null){
            context = new Context();
            threadLocal.set(context);
        }
        return threadLocal.get();
    }

    public static BaseResponse baseResponse(){
        Context context = getContext();
        BaseResponse baseResponse = context.baseResponse;
        if(baseResponse == null){
            context.baseResponse = new BaseResponse();
        }
        return context.baseResponse;
    }

    public static boolean isInterrupted(){
        return getContext().interrupted;
    }

    private static Map<String,Object> temporaryArgs(){
        Context context = getContext();
        Map<String,Object> temporaryArgs = context.temporaryArgs;
        if(temporaryArgs == null){
            context.temporaryArgs = new HashMap<>();
        }
        return context.temporaryArgs;
    }

    public static void saveTemporaryArgs(String key,Object arg){
        temporaryArgs().put(key,arg);
    }

    public static Object queryTemporaryArgs(String key){
        return temporaryArgs().get(key);
    }

    public static void injectError(String errorCode,String errorMsg){
        getContext().interrupted = Boolean.TRUE;
        BaseResponse baseResponse = baseResponse();
        baseResponse.setData(null);
        baseResponse.setErrorMsg(errorMsg);
        baseResponse.setErrorCode(errorCode);
    }

    public static void injectData(Object data){
        getContext().interrupted = Boolean.TRUE;
        BaseResponse baseResponse = baseResponse();
        baseResponse.setData(data);
        baseResponse.setErrorMsg(null);
        baseResponse.setErrorCode(null);
    }

    public static void clear(){
        getThreadLocal().remove();
        CACHE.remove(Thread.currentThread());
    }

}
