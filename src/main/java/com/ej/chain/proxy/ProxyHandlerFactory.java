package com.ej.chain.proxy;

import com.ej.chain.annotation.FromContext;
import com.ej.chain.annotation.ToContext;
import com.ej.chain.context.ChainContext;
import com.ej.chain.handlers.Handler;
import javassist.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProxyHandlerFactory {
    private static final Map<Class<? extends Handler>, Object> CGLIB_OBJ_CACHE = new HashMap<>();
    private static final Map<Class<? extends Handler>, Object> JAVASSIST_OBJ_CACHE = new HashMap<>();
    //private static final Map<Class<? extends Handler>, Class<?>> JAVASSIST_CLASS_CACHE = new HashMap<>();

    public static class CglibProxyHandler<Request> implements MethodInterceptor {
        private Class<? extends Handler> clazz;

        public CglibProxyHandler(Class<? extends Handler> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            FromContext fromContext = null;
            ToContext toContext = null;
            if ((fromContext = method.getAnnotation(FromContext.class)) != null) {
                return ChainContext.queryTemporaryArgs(fromContext.value());
            } else if ((toContext = method.getAnnotation(ToContext.class)) != null) {
                ChainContext.saveTemporaryArgs(toContext.value(), args[0]);
                return null;
            }
            return proxy.invokeSuper(obj, args);
        }
    }

    public static <Request> Handler<Request> getCglibProxyHandler(Class<? extends Handler> clazz) {
        checkClass(clazz);
        List<Method> methods = getHasAnnotationMethods(clazz);
        checkMethods(clazz, methods);
        synchronized (clazz) {
            if (CGLIB_OBJ_CACHE.containsKey(clazz)) {
                return (Handler<Request>) CGLIB_OBJ_CACHE.get(clazz);
            }
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(new CglibProxyHandler(clazz));
            CGLIB_OBJ_CACHE.put(clazz, enhancer.create());
            return (Handler<Request>) CGLIB_OBJ_CACHE.get(clazz);
        }
    }


    private static final String EXTENDS_CLASS_NAME_SUFFIX = "ProxyHandler";
    private static final String ABSTRACT_METHOD_KEY = "abstract";
    private static final String FROM_METHOD_TEMPLATE = "%s %s %s(){return (%s)com.ej.chain.context.ChainContext.queryTemporaryArgs(\"%s\");}";
    private static final String TO_METHOD_TEMPLATE = "%s void %s(%s object){com.ej.chain.context.ChainContext.saveTemporaryArgs(\"%s\",object);}";


    public static <Request> Handler<Request> getJavassistProxyHandler(Class<? extends Handler> clazz) {
        checkClass(clazz);
        List<Method> methods = getHasAnnotationMethods(clazz);
        checkMethods(clazz, methods);
        synchronized (clazz) {
            if (JAVASSIST_OBJ_CACHE.containsKey(clazz)) {
                return (Handler<Request>) JAVASSIST_OBJ_CACHE.get(clazz);
            }
            String className = clazz.getName() + EXTENDS_CLASS_NAME_SUFFIX;
            try {
                Class<?> proxyClass = buildExtendsClass(clazz, className, methods);
                Constructor<?> constructor = proxyClass.getConstructor();
                JAVASSIST_OBJ_CACHE.put(clazz,constructor.newInstance());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return (Handler<Request>) JAVASSIST_OBJ_CACHE.get(clazz);
        }
    }

    private static Class<?> buildExtendsClass(Class<?> targetClass, String className, List<Method> methods) throws NotFoundException, CannotCompileException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.makeClass(className);
        cc.setSuperclass(cp.get(targetClass.getName()));
        //实现抽象方法
        for (Method method : methods) {
            String methodString = buildMethod(method);
            CtMethod cm = CtMethod.make(methodString, cc);
            cc.addMethod(cm);
        }
        return cc.toClass();
    }

    private static String buildMethod(Method method) {
        String openLevel = Modifier.toString(method.getModifiers());
        int idx = 0;
        if ((idx = openLevel.indexOf(ABSTRACT_METHOD_KEY)) >= 0) {
            openLevel = openLevel.substring(0, idx);
        }
        String methodName = method.getName();
        String key = null;
        if(method.getAnnotation(FromContext.class) != null){
            key = method.getAnnotation(FromContext.class).value();
            return String.format(FROM_METHOD_TEMPLATE,openLevel,method.getReturnType().getName(),methodName,method.getReturnType().getName(),key);
        }else {
            key = method.getAnnotation(ToContext.class).value();
            return String.format(TO_METHOD_TEMPLATE,openLevel,methodName,method.getParameterTypes()[0].getName(),key);
        }
    }


    private static void checkClass(Class<? extends Handler> clazz) {
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException(clazz.getName() + " is not an abstract class");
        }
    }

    private static List<Method> getHasAnnotationMethods(Class<? extends Handler> clazz) {
        return Arrays.asList(clazz.getDeclaredMethods()).stream().filter(method -> {
            return method.getAnnotation(FromContext.class) != null || method.getAnnotation(ToContext.class) != null;
        }).collect(Collectors.toList());
    }

    private static void checkMethods(Class<? extends Handler> clazz, List<Method> methods) {
        if (methods == null || methods.size() == 0) {
            throw new IllegalArgumentException(clazz.getName() + " does't have any abstract methods");
        }
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                throw new IllegalArgumentException(method.toString() + " is not an abstract method");
            }
            if (method.getAnnotation(FromContext.class) != null && method.getAnnotation(ToContext.class) != null) {
                throw new IllegalArgumentException(method.toString() + " can't have both FromContext and ToContext");
            }
            if (method.getAnnotation(FromContext.class) != null) {
                if (method.getReturnType() == void.class) {
                    throw new IllegalArgumentException(method.toString() + "  must have a return value");
                }
                if (method.getParameterTypes().length != 0) {
                    throw new IllegalArgumentException(method.toString() + " can't have arguments");
                }

            }
            if (method.getAnnotation(ToContext.class) != null) {
                if (method.getReturnType() != void.class) {
                    throw new IllegalArgumentException(method.toString() + " can't have a return value");
                }
                if (method.getParameterTypes().length != 1) {
                    throw new IllegalArgumentException(method.toString() + " must have an argument");
                }
            }
        }
    }




}
