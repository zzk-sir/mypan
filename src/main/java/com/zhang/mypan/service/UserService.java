package com.zhang.mypan.service;

import com.zhang.mypan.dto.Result;
import com.zhang.mypan.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author 86180
 * @description 针对表【user_info】的数据库操作Service
 * @createDate 2024-03-16 20:39:37
 */
public interface UserService extends IService<User> {

    void getCode(HttpServletRequest request, HttpServletResponse response);

    Result register(String email, String nickName, String password, String emailCode);

    Result login(String email, String password);

    Result resetPwd(String email, String password, String emailCode);

    void getAvatarIcon(HttpServletResponse response, String userId);

    Result getUserInfo();

    Result getUserSpace();

    Result logout();

    Result updateUserAvatar(MultipartFile avatar);

    Result qqlogin(HttpSession session, String callbackUrl);

    Result qqloginCollback(HttpSession session, String code, String state);

    void setUserSpace(String userId, Long useSpace, Long totalSpace);

    Result updatePassword(String password);

    Result loadUserList(Integer pageNo, Integer pageSize, Integer status, String nickNameFuzzy);

    Result updateUserStatus(String userId, Integer status);

    void deleteUserSpace(String userId);


}
