package com.bit.api.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author: starcold
 * createTime: 2020/9/11 7:18
 * context: 告诉我们这个方法需要向外暴露出去
 * updateTime:
 * updateContext:
 */
@Target({ElementType.METHOD})//用于描述方法
//注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMapping {
    String value();
    boolean userLogin() default false;
}
