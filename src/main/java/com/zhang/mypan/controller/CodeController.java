package com.zhang.mypan.controller;

import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.enums.RegexEnum;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.service.EmailCodeService;
import com.zhang.mypan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@RestController
public class CodeController {
    private UserService userService;
    private EmailCodeService emailCodeService;

    /**
     * 获取图片验证码
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/checkCode")
    public void getCode(HttpServletRequest request, HttpServletResponse response) {
        userService.getCode(request, response);
    }

    /**
     * 发送邮箱验证码
     *
     * @param session
     * @param email
     * @param checkCode
     * @param type
     * @return
     */
    @GlobalIntercepter(checkParam = true)
    @PostMapping("/sendEmailCode")
    public Result sendEmailCode(HttpSession session,
                                @VerifyParam(required = true, regex = RegexEnum.EMAIL) String email,
                                @VerifyParam(required = true) String checkCode,
                                @VerifyParam(required = true) Integer type) {
        return emailCodeService.sendEmailCode(session, email, checkCode, type);
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setEmailCodeService(EmailCodeService emailCodeService) {
        this.emailCodeService = emailCodeService;
    }
}
