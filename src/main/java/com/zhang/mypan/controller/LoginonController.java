package com.zhang.mypan.controller;

import cn.hutool.core.util.ObjectUtil;
import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.enums.RegexEnum;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.service.UserService;
import com.zhang.mypan.utils.SessionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class LoginonController {
    private UserService userService;

    /**
     * 注册
     *
     * @param session
     * @param email
     * @param nickName
     * @param password
     * @param checkCode
     * @param emailCode
     * @return
     */
    @PostMapping("/register")
    @GlobalIntercepter(checkParam = true)
    public Result register(HttpSession session,
                           @VerifyParam(required = true, regex = RegexEnum.EMAIL) String email,
                           @VerifyParam(required = true) String nickName,
                           @VerifyParam(required = true) String password,
                           @VerifyParam(required = true) String checkCode,
                           @VerifyParam(required = true) String emailCode) {
        try {
            final Object img_code = session.getAttribute(SessionConstants.IMAGE_CODE);
            if (ObjectUtil.isEmpty(img_code) || !checkCode.equals(img_code.toString())) {
                throw new MyPanException("图片验证码错误", CodeEnum.BAD_REQUEST);
            }
            //  注册逻辑
            return userService.register(email, nickName, password, emailCode);
        } finally {
            session.removeAttribute(SessionConstants.IMAGE_CODE);
        }
    }

    /**
     * 登录
     *
     * @param session
     * @param email
     * @param password
     * @param checkCode
     * @return
     */
    @PostMapping("/login")
    public Result login(HttpSession session,
                        @VerifyParam(required = true) String email,
                        @VerifyParam(required = true) String password,
                        @VerifyParam(required = true) String checkCode) {
        try {
            final Object img_code = session.getAttribute(SessionConstants.IMAGE_CODE);
            if (ObjectUtil.isEmpty(img_code) || !checkCode.equals(img_code.toString())) {
                throw new MyPanException("图片验证码错误", CodeEnum.BAD_REQUEST);
            }
            //  登录逻辑
            return userService.login(email, password);
        } finally {
            session.removeAttribute(SessionConstants.IMAGE_CODE);
        }
    }


    /**
     * 重置密码
     *
     * @param session
     * @param email
     * @param password
     * @param emailCode
     * @return
     */
    @PostMapping("/resetPwd")
    public Result resetPwd(HttpSession session,
                           @VerifyParam(required = true) String email,
                           @VerifyParam(required = true) String password,
                           @VerifyParam(required = true) String checkCode,
                           @VerifyParam(required = true) String emailCode) {
        try {
            final Object img_code = session.getAttribute(SessionConstants.IMAGE_CODE);
            if (ObjectUtil.isEmpty(img_code) || !checkCode.equals(img_code.toString())) {
                throw new MyPanException("图片验证码错误", CodeEnum.BAD_REQUEST);
            }
            //  重置密码业务
            return userService.resetPwd(email, password, emailCode);
        } finally {
            session.removeAttribute(SessionConstants.IMAGE_CODE);
        }
    }

    /**
     * QQ登录
     * 暂不支持 没有备案的域名 😂
     *
     * @param session
     * @param callbackUrl
     * @return
     */
    @PostMapping("/qqlogin")
    public Result qqlogin(HttpSession session, String callbackUrl) {
        return userService.qqlogin(session, callbackUrl);
    }

    /**
     * QQ登录成功回调
     * 暂不支持 没有备案的域名 😂
     *
     * @param session
     * @param code
     * @param state
     * @return
     */
    @PostMapping("/qqlogin/callback")
    @GlobalIntercepter(checkParam = true)
    public Result qqloginCallback(HttpSession session,
                                  @VerifyParam(required = true) String code,
                                  @VerifyParam(required = true) String state) {
        return userService.qqloginCollback(session, code, state);
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
