package com.zhang.mypan.controller;

import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
public class UserController {
    private UserService userService;

    /**
     * 获取用户头像
     * 头像路径 basepath+/file/avater/userId{default}/
     * 如果是默认的头像地址可以放多个不同格式的图片（必须为图片）
     * 用户也可以自定义不同类型的头像，但是只能有一个（设置用户头像时删除之前的图片）
     *
     * @param response
     * @param userId
     * @return
     */
    @GetMapping("/getAvatar/{userId}")
    @GlobalIntercepter(checkParam = true)
    public void getAvatarIcon(HttpServletResponse response,
                              @VerifyParam(required = true)
                              @PathVariable("userId") String userId) {
        // 获取用户头像
        userService.getAvatarIcon(response, userId);
    }


    /**
     * 获取用户信息
     *
     * @return
     */
    @PostMapping("/getUserInfo")
    public Result getUserInfo() {
        // 获取用户信息
        return userService.getUserInfo();
    }

    /**
     * 获取用户使用空间大小
     *
     * @return
     */

    @PostMapping("/getUseSpace")
    public Result getUserSpace() {
        // 获取用户使用空间大小
        return userService.getUserSpace();
    }

    /**
     * 修改密码
     *
     * @param password
     * @return
     */
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestParam("password") String password) {
        // 修改密码
        return userService.updatePassword(password);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    public Result logout() {
        // 退出登录
        return userService.logout();
    }

    /**
     * 更新用户头像
     *
     * @param avatar
     * @return
     */
    // 更新用户头像之前要先获取
    // 将图片设置在web容器外面，不允许访问，可以避免一些图片木马操作
    // 这里其实是为了效率，这样做，如果可以，要重写这个图片，加上水印，删除之前的木马图片
    @PostMapping("/updateUserAvatar")
    public Result updateUserAvatar(@RequestParam("avatar") MultipartFile avatar) {
        // 更新用户头像
        return userService.updateUserAvatar(avatar);
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
