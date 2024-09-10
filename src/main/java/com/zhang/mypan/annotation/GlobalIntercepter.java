package com.zhang.mypan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * 参数拦截器
 */
@Target({ElementType.METHOD}) // 注解应用的范围
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalIntercepter {
    /**
     * 是否校验参数
     *
     * @return
     */
    boolean checkParam() default false;

    /**
     * 校验是否为超级管理员
     */
    boolean checkAdmin() default false;
}
