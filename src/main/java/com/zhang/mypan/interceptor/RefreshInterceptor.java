package com.zhang.mypan.interceptor;

import cn.hutool.core.util.StrUtil;
import com.zhang.mypan.dto.UserDTO;
import com.zhang.mypan.utils.JwtUtil;
import com.zhang.mypan.utils.RedisConstants;
import com.zhang.mypan.utils.RedisUtil;
import com.zhang.mypan.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RefreshInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;

    public RefreshInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 查看是否存在用户
        String token = request.getHeader("authorization");
        String userId = null;
        System.out.println("请求方法：" + request.getRequestURL().toString());
        try {
            userId = JwtUtil.parseJWT(token).getSubject();
            // 解码
            String key = RedisConstants.USER_LOGIN_KEY + userId;
            UserDTO user = UserHolder.getUser();
            if (StrUtil.isNotBlank(token) && user != null) {
                //  存在,刷新token
                System.out.println("刷新token存活时间");
                redisUtil.expire(key, RedisConstants.USER_LOGIN_TTL);
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
