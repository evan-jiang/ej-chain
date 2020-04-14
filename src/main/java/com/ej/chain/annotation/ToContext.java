package com.ej.chain.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要将数据写入到责任链上下文时使用，用在抽象方法上
 *
 * @author: Evan·Jiang
 * @date: 2020/4/14 15:12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToContext {

    /**
     * 将数据写入责任链上下文时需要的key<br/>
     * 与{@link FromContext}的value配合使用
     */
    String value();
}
