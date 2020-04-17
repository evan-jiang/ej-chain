package com.ej.chain.proxy;

import com.ej.chain.annotation.FromContext;
import com.ej.chain.annotation.ToContext;
import com.ej.chain.context.ChainContext;
import com.ej.chain.handlers.Handler;
import javassist.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * 抽象Handler代理工厂
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 16:24
 */
public class ProxyHandlerFactory {

    /**
     * JAVASSIST生成的代理对象池
     */
    private static final Map<Class<?>, Object> JAVASSIST_OBJ_CACHE = new ConcurrentHashMap<>();
    /**
     * JAVASSIST生成的代理类池
     */
    private static final Map<Class<?>, Class<?>> JAVASSIST_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取抽象Handler的JAVASSIST代理对象
     *
     * @param clazz
     * @return T
     * @auther: Evan·Jiang
     * @date: 2020/4/15 14:03
     */
    public static <T extends Handler> T getJavassistProxyHandlerInstance(Class<T> clazz, boolean singleton) {
        Class<?> proxyClass = getJavassistProxyHandlerClass(clazz);
        if (!singleton) {
            try {
                return (T) proxyClass.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (JAVASSIST_OBJ_CACHE.containsKey(clazz)) {
            return (T) JAVASSIST_OBJ_CACHE.get(clazz);
        }
        synchronized (clazz) {
            if (JAVASSIST_OBJ_CACHE.containsKey(clazz)) {
                return (T) JAVASSIST_OBJ_CACHE.get(clazz);
            }
            try {
                JAVASSIST_OBJ_CACHE.put(clazz, proxyClass.newInstance());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return (T) JAVASSIST_OBJ_CACHE.get(clazz);
        }
    }

    private static final String EXTENDS_CLASS_NAME_SUFFIX = "ProxyHandler";
    private static final String ABSTRACT_METHOD_KEY = "abstract";
    private static final String FROM_METHOD_TEMPLATE = "%s %s %s(){return (%s)%s.extractTemporaryArgs(\"%s\");}";
    private static final String TO_METHOD_TEMPLATE = "%s void %s(%s object){%s.injectTemporaryArgs(\"%s\",object);}";


    /**
     * 获取抽象Handler的JAVASSIST代理类
     *
     * @param clazz
     * @return java.lang.Class<?>
     * @auther: Evan·Jiang
     * @date: 2020/4/17 10:49
     */
    public static Class<?> getJavassistProxyHandlerClass(Class<?> clazz) {
        if (JAVASSIST_CLASS_CACHE.containsKey(clazz)) {
            return JAVASSIST_CLASS_CACHE.get(clazz);
        }
        synchronized (clazz) {
            if (JAVASSIST_CLASS_CACHE.containsKey(clazz)) {
                return JAVASSIST_CLASS_CACHE.get(clazz);
            }
            checkClass(clazz);
            List<Method> methods = getAbstractMethods(clazz);
            checkMethods(clazz, methods);
            String className = clazz.getName() + EXTENDS_CLASS_NAME_SUFFIX;
            try {
                Class<?> proxyClass = buildExtendsClass(clazz, className, methods);
                JAVASSIST_CLASS_CACHE.put(clazz, proxyClass);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return JAVASSIST_CLASS_CACHE.get(clazz);
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
            return String.format(FROM_METHOD_TEMPLATE, openLevel, method.getReturnType().getName(), methodName, method.getReturnType().getName(), ChainContext.class.getName(), key);
        } else {
            key = method.getAnnotation(ToContext.class).value();
            return String.format(TO_METHOD_TEMPLATE, openLevel, methodName, method.getParameterTypes()[0].getName(), ChainContext.class.getName(), key);
        }
    }

    /**
     * 对需要代理的Handler类的校验
     *
     * @param clazz
     * @auther: Evan·Jiang
     * @date: 2020/4/14 16:30
     */
    private static void checkClass(Class<?> clazz) {
        if (!Handler.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz.getName() + " must be the child of " + Handler.class.getName());
        }
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException(clazz.getName() + " is not an abstract class");
        }
    }

    /**
     * 获取需要代理的Handler类的所有抽象方法
     *
     * @param clazz
     * @return java.util.List<java.lang.reflect.Method>
     * @auther: Evan·Jiang
     * @date: 2020/4/15 9:51
     */
    private static List<Method> getAbstractMethods(Class<?> clazz) {
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
    private static void checkMethods(Class<?> clazz, List<Method> methods) {
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
            } else {
                throw new IllegalArgumentException(method.toString() + " need either FromContext or ToContext");
            }
        }
    }


}
