package com.zhang.mypan.service;

import com.zhang.mypan.dto.Result;

import javax.servlet.http.HttpSession;

/**
 * @author 86180
 * @description 针对表【email_code】的数据库操作Service
 * @createDate 2024-03-17 10:05:48
 */
public interface EmailCodeService {

    Result sendEmailCode(HttpSession session, String email, String checkCode, Integer type);
}
