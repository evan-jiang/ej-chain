package com.ej.chain.proxy;

import com.ej.chain.annotation.FromContext;
import com.ej.chain.annotation.ToContext;
import com.ej.chain.context.ChainContext;
import com.ej.chain.handlers.Handler;
import javassist.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 抽象Handler代理工厂
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 16:24
 */
public class ProxyHandlerFactory {
    /**
     * CGLIB生成的代理对象池
     */
    private static final Map<Class<? extends Handler>, Object> CGLIB_OBJ_CACHE = new HashMap<>();
    /**
     * JAVASSIST生成的代理对象池
     */
    private static final Map<Class<? extends Handler>, Object> JAVASSIST_OBJ_CACHE = new HashMap<>();


    /**
     * CGLIB代理对象的代理方法执行类
     *
     * @author: Evan·Jiang
     * @date: 2020/4/14 16:26
     */
    public static class CglibProxyHandler<Request> implements MethodInterceptor {
        /**
         * 被代理的抽象Handler的Class
         */
        private Class<? extends Handler> clazz;

        public CglibProxyHandler(Class<? extends Handler> clazz) {
            this.clazz = clazz;
        }

        /**
         * CGLIB代理方法类执行体
         *
         * @param obj
         * @param method
         * @param args
         * @param proxy
         * @return java.lang.Object
         * @auther: Evan·Jiang
         * @date: 2020/4/14 16:27
         */
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            FromContext fromContext = null;
            ToContext toContext = null;
            if ((fromContext = method.getAnnotation(FromContext.class)) != null) {
                return ChainContext.extractTemporaryArgs(fromContext.value());
            } else if ((toContext = method.getAnnotation(ToContext.class)) != null) {
                ChainContext.injectTemporaryArgs(toContext.value(), args[0]);
                return null;
            }
            return proxy.invokeSuper(obj, args);
        }
    }

    /**
     * 获取抽象Handler的CGLIB代理对象
     *
     * @param clazz
     * @return com.ej.chain.handlers.Handler<Request>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:28
     */
    public static <Request> Handler<Request> getCglibProxyHandler(Class<? extends Handler> clazz) {
        checkClass(clazz);
        List<Method> methods = getAbstractMethods(clazz);
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
    private static final String FROM_METHOD_TEMPLATE = "%s %s %s(){return (%s)com.ej.chain.context.ChainContext.extractTemporaryArgs(\"%s\");}";
    private static final String TO_METHOD_TEMPLATE = "%s void %s(%s object){com.ej.chain.context.ChainContext.injectTemporaryArgs(\"%s\",object);}";

    /**
     * 获取抽象Handler的JAVASSIST代理对象
     *
     * @param clazz
     * @return com.ej.chain.handlers.Handler<Request>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:28
     */
    public static <Request> Handler<Request> getJavassistProxyHandler(Class<? extends Handler> clazz) {
        checkClass(clazz);
        List<Method> methods = getAbstractMethods(clazz);
        checkMethods(clazz, methods);
        synchronized (clazz) {
            if (JAVASSIST_OBJ_CACHE.containsKey(clazz)) {
                return (Handler<Request>) JAVASSIST_OBJ_CACHE.get(clazz);
            }
            String className = clazz.getName() + EXTENDS_CLASS_NAME_SUFFIX;
            try {
                Class<?> proxyClass = buildExtendsClass(clazz, className, methods);
                Constructor<?> constructor = proxyClass.getConstructor();
                JAVASSIST_OBJ_CACHE.put(clazz, constructor.newInstance());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return (Handler<Request>) JAVASSIST_OBJ_CACHE.get(clazz);
        }
    }

    /**
     * 生成抽象Handler的JAVASSIST的代理类
     *
     * @param targetClass
     * @param className
     * @param methods
     * @return java.lang.Class<?>
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:29
     */
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

    /**
     * 生成抽象Handler的JAVASSIST代理类的代理方法
     *
     * @param method
     * @return java.lang.String
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:29
     */
    private static String buildMethod(Method method) {
        String openLevel = Modifier.toString(method.getModifiers());
        int idx = 0;
        if ((idx = openLevel.indexOf(ABSTRACT_METHOD_KEY)) >= 0) {
            openLevel = openLevel.substring(0, idx);
        }
        String methodName = method.getName();
        String key = null;
        if (method.getAnnotation(FromContext.class) != null) {
            key = method.getAnnotation(FromContext.class).value();
            return String.format(FROM_METHOD_TEMPLATE, openLevel, method.getReturnType().getName(), methodName, method.getReturnType().getName(), key);
        } else {
            key = method.getAnnotation(ToContext.class).value();
            return String.format(TO_METHOD_TEMPLATE, openLevel, methodName, method.getParameterTypes()[0].getName(), key);
        }
    }

    /**
     * 对需要代理的Handler类的校验
     *
     * @param clazz
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:30
     */
    private static void checkClass(Class<? extends Handler> clazz) {
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException(clazz.getName() + " is not an abstract class");
        }
    }

    /**
     * 获取需要代理的Handler类的所有抽象方法
     * @param clazz
     * @return java.util.List<java.lang.reflect.Method>
     * @auther: Evan·Jiang
     * @date: 2020/4/15 9:51
     */
    private static List<Method> getAbstractMethods(Class<? extends Handler> clazz) {
        return Arrays.asList(clazz.getDeclaredMethods()).stream().filter(method -> Modifier.isAbstract(method.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 对需要代理的Handler类使用了{@link FromContext}或{@link ToContext}注解的抽象方法进行校验
     *
     * @param clazz
     * @param methods
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:31
     */
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
            } else if (method.getAnnotation(FromContext.class) != null) {
                if (method.getReturnType() == void.class) {
                    throw new IllegalArgumentException(method.toString() + "  must have a return value");
                }
                if (method.getParameterTypes().length != 0) {
                    throw new IllegalArgumentException(method.toString() + " can't have arguments");
                }
            } else if (method.getAnnotation(ToContext.class) != null) {
                if (method.getReturnType() != void.class) {
                    throw new IllegalArgumentException(method.toString() + " can't have a return value");
                }
                if (method.getParameterTypes().length != 1) {
                    throw new IllegalArgumentException(method.toString() + " must have an argument");
                }
            }else {
                throw new IllegalArgumentException(method.toString() + " need either FromContext or ToContext");
            }
        }
    }


}
