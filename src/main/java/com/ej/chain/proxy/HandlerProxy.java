package com.ej.chain.proxy;

import com.ej.chain.annotation.FromContext;
import com.ej.chain.annotation.ToContext;
import com.ej.chain.context.ChainContext;
import com.ej.chain.handlers.Handler;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HandlerProxy<Request> implements MethodInterceptor {
    private Class<? extends Handler> clazz;

    public HandlerProxy(Class<? extends Handler> clazz) {
        this.clazz = clazz;
    }

    public Handler<Request> getObject() {
        check();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        return (Handler<Request>)enhancer.create();
    }

    private void check() {
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException(clazz.getName() + " is not an abstract class");
        }
        List<Method> fromContextMethods = getMethods(clazz, FromContext.class);
        for (Method method : fromContextMethods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                throw new IllegalArgumentException(method.toString() + " is not an abstract method");
            }
        }
        List<Method> toContextMethods = getMethods(clazz, ToContext.class);
        for (Method method : toContextMethods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                throw new IllegalArgumentException(method.toString() + " is not an abstract method");
            }
            if (fromContextMethods.contains(method)) {
                throw new IllegalArgumentException(method.toString() + " can't have both FromContext and ToContext");
            }
        }
    }

    private List<Method> getMethods(Class<?> clazz, final Class<? extends Annotation> methodAnnotation) {
        return Arrays.asList(clazz.getDeclaredMethods()).stream().filter(method -> {
            return method.getAnnotation(methodAnnotation) != null;
        }).collect(Collectors.toList());
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        FromContext fromContext = null;
        ToContext toContext = null;
        if ((fromContext = method.getAnnotation(FromContext.class)) != null) {
            return ChainContext.queryTemporaryArgs(fromContext.value());
        } else if ((toContext = method.getAnnotation(ToContext.class)) != null) {
            ChainContext.saveTemporaryArgs(toContext.value(), objects[0]);
            return null;
        }
        return methodProxy.invokeSuper(o, objects);
    }
}
