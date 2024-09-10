package com.zhang.mypan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhang.mypan.config.AppConfig;
import com.zhang.mypan.entity.User;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class UserInfo {
    private String userId;

    private String email;

    private String nickName;

    private String qqAvatar;

    private String password;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Long useSpace;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    private Long totalSpace;
    private Boolean isAdmin;

    public UserInfo() {

    }

    public UserInfo(User user, AppConfig appConfig) {
        userId = user.getUserId();
        nickName = user.getNickName();
        email = user.getEmail();
        qqAvatar = user.getQqAvatar();
        password = user.getPassword();
        createTime = user.getCreateTime();
        useSpace = user.getUseSpace();
        status = user.getStatus();
        lastLoginTime = user.getLastLoginTime();
        totalSpace = user.getTotalSpace();
        isAdmin = ArrayUtils.contains(appConfig.getAdmin(), user.getEmail());
    }
}
