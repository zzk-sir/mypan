package com.zhang.mypan.interceptor;

import cn.hutool.core.util.StrUtil;
import com.zhang.mypan.dto.UserDTO;
import com.zhang.mypan.utils.JwtUtil;
import com.zhang.mypan.utils.RedisConstants;
import com.zhang.mypan.utils.RedisUtil;
import com.zhang.mypan.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;

    public LoginInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
    // 使用ThreadLocal+redis实现session
    // 请求从redius中获取user，存入ThreadLocal,方便其他程序调用
    // 请求之后清除ThreadLocal

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1获取请求头的token

        final String token = request.getHeader("authorization");
        System.out.println(request.getRequestURI());
        System.out.println("token = " + token);
        if (StrUtil.isBlank(token)) {
            // 不存在 ，拦截 ，返回 401
            response.setStatus(401);
            return false;
        }
        // 2.基于token获取redis获取用户
        String userId = null;
        try {
            userId = JwtUtil.parseJWT(token).getSubject();
        } catch (Exception e) {
            // jwt 异常
            log.error("解析token失败！", e);
            response.setStatus(401);
            return false;
        }
        String key = RedisConstants.USER_LOGIN_KEY + userId;
        UserDTO userDTO = redisUtil.hmgetToObj(key, new UserDTO());

        UserDTO user = UserHolder.getUser();
        // 3.判断用户是否存在
        if (Objects.isNull(userDTO)) {
            // 4.不存在，拦截
            response.setStatus(901); // 登录超时
            if (null == user) {
                response.setStatus(401); // 未登录
            }
            return false;
        }
        // 6.将UserDTO对象存入ThreadLocal
        UserHolder.saveUser(userDTO);
        // 7.刷新user有效期
        redisUtil.expire(key, RedisConstants.USER_LOGIN_TTL);

        System.out.println("userDTO" + UserHolder.getUser());
        // 8.放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 清除ThreadLocal 防止内存泄漏和共享
        UserHolder.removeUser();
    }
}
