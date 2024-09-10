package com.zhang.mypan.annotation;


import com.zhang.mypan.enums.RegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface VerifyParam {
    // 最小值
    int min() default -1;

    // 最大值
    int max() default Integer.MAX_VALUE;

    // 是否必须
    boolean required() default false;

    // 正则表达式
    RegexEnum regex() default RegexEnum.NO;

}
