package com.zhang.mypan.config;

import com.zhang.mypan.interceptor.LoginInterceptor;
import com.zhang.mypan.interceptor.RefreshInterceptor;
import com.zhang.mypan.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MVCConfig implements WebMvcConfigurer {
    private RedisUtil redisUtil;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //// 先添加先执行， 或者设置order 小的先执行
        System.out.println("开始拦截");
        // 先拦截所有请求 => 检测token
        // 这些需要登录 如果登录了设置 刷新token存活
        registry.addInterceptor(new RefreshInterceptor(redisUtil));

        // 不需要登录的 不用刷新token存活期
        // 在拦截部分 判断是否存在user 存在
        registry.addInterceptor(new LoginInterceptor(redisUtil))
                .excludePathPatterns(  // 设置不拦截的路径
                        "/register", // 注册
                        "/checkCode", // 获取图片验证码
                        "/sendEmailCode", // 发送邮箱验证码
                        "/login", // 登录
                        "/resetPwd", //重置密码
                        "/error",
                        "/getAvatar/**",
                        "/file/getImage/**",
                        "/file/download/**", // 下载文件
                        "/admin/download/**",
                        "/showShare/download/**"
//                        "/swagger-resources/**", // 这三个都是swagger-ui的资源
//                        "/swagger-ui/**",
//                        "/v3/**",
//                        "/doc.html",  // 不拦截swagger美化的
//                        "/webjars/**",
//                        "img/ico.png",
//                        "favicon.ico"
                );
    }

    // 不要使用 继承了继承了DelegatingWebMvcConfiguration，或者WebMvcConfigurationSupport
    // 因为会覆盖掉WebMvcAutoConfiguration的配置
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html", "doc.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//        registry.addResourceHandler("/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//                .resourceChain(false);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}
