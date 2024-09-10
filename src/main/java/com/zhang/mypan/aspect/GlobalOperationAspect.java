package com.zhang.mypan.aspect;

import cn.hutool.core.util.StrUtil;
import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.dto.UserDTO;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {
    public static final String TYPE_STRING = "java.lang.String";
    public static final String TYPE_INTERGER = "java.lang.Interger";
    public static final String TYPE_LONG = "java.lang.Long";

    // 定义切点
    // 用于拦截 有@GlobalIntercepter注解的方法
    @Pointcut("@annotation(com.zhang.mypan.annotation.GlobalIntercepter)")
    private void requestIntercepter() {

    }

    @Before("requestIntercepter()")
    public void intercepter(JoinPoint point) throws MyPanException {
        // 具体的目标
        Object target = point.getTarget();
        // 参数
        Object[] args = point.getArgs();
        // 方法名
        String mothedName = point.getSignature().getName();
        // 参数类型
        Class<?>[] parametersType = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
        // 方法
        Method method = null;
        try {
            method = target.getClass().getMethod(mothedName, parametersType);
        } catch (NoSuchMethodException e) {
            log.error("aop,获取方法失败");
            throw new MyPanException();
        }
        // 拦截器
        GlobalIntercepter intercepter = method.getAnnotation(GlobalIntercepter.class);
        if (null == intercepter) {
            return;
        }
        /**
         *  校验参数
         */
        if (intercepter.checkParam()) {
            validateParams(method, args);
        }
        //校验是否为admin
        if (intercepter.checkAdmin()) {
            UserDTO user = UserHolder.getUser();
            if (null == user || !user.getIsAdmin()) {
                throw new MyPanException("您没有访问权限", CodeEnum.NO_PERMISSION);
            }
        }

    }

    /**
     * 检验参数
     *
     * @param method
     * @param args
     */
    private void validateParams(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                continue;
            }
            // 基本参数类型
            if (TYPE_STRING.equals(parameter.getParameterizedType().getTypeName()) ||
                    TYPE_INTERGER.equals(parameter.getParameterizedType().getTypeName()) ||
                    TYPE_LONG.equals(parameter.getParameterizedType().getTypeName())) {
                checkValue(arg, verifyParam);
            } else {
                // 检测对象类型
                checkObjValue(parameter, arg);
            }
        }
    }

    /**
     * 检验对象
     *
     * @param parameter
     * @param arg
     */
    private void checkObjValue(Parameter parameter, Object arg) {
        try {
            String typeName = parameter.getParameterizedType().getTypeName();
            Class classz = Class.forName(typeName);
            Field[] fields = classz.getDeclaredFields();
            for (Field field : fields) {
                VerifyParam verifyParam = field.getAnnotation(VerifyParam.class);
                if (verifyParam == null) {
                    continue;
                }
                field.setAccessible(true);
                Object resultValue = field.get(arg);
                checkValue(resultValue, verifyParam);
            }
        } catch (Exception e) {
            log.error("参数校验异常", e);
            throw new MyPanException("参数错误");
        }
    }

    /**
     * 检验基本变量
     *
     * @param arg
     * @param verifyParam
     */
    private void checkValue(Object arg, VerifyParam verifyParam) {
        Boolean isEmpty = arg == null || StrUtil.isEmpty(arg.toString());
        Integer length = arg == null ? 0 : arg.toString().length();

        /**
         * 检验空
         */
        if (isEmpty && verifyParam.required()) {
            log.error("没有设置参数要校验required属性为true");
            throw new MyPanException("参数错误", CodeEnum.BAD_REQUEST);
        }

        /**
         * 检验长度
         */
        if (!isEmpty && (verifyParam.max() != Integer.MAX_VALUE && length < verifyParam.max() ||
                verifyParam.min() != -1 && length > verifyParam.min())) {
            log.error("参数长度异常");
            throw new MyPanException("参数错误", CodeEnum.BAD_REQUEST);
        }
        /**
         * 校验正则
         */
        if (!isEmpty && StrUtil.isNotEmpty(verifyParam.regex().getRegex()) &&
                !String.valueOf(arg).matches(verifyParam.regex().getRegex())) {
            log.error("参数正则校验异常");
            throw new MyPanException(verifyParam.regex().getErrmsg(), CodeEnum.BAD_REQUEST);
        }
    }


}
