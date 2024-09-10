package com.zhang.mypan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {

    @Value("${spring.mail.username}")
    private String sendUserName;
    @Value("${admin.emails}")
    private String[] admin;
    @Value("${project.folder}")
    private String ProjectFolder;

    // qq登录相关
    @Value("${qq.app.id}")
    private String qqAppId;

    @Value("${qq.app.key}")
    private String qqAppKey;

    @Value("${qq.url.authorization}")
    private String qqUrlAuthrization;

    @Value("${qq.url.access.token}")
    private String qqUrlAccessToken;
    @Value("${qq.url.openid}")
    private String qqUrlOpenId;

    @Value("${qq.url.user.info}")
    private String qqUrlUserInfo;

    @Value("${qq.url.redirect}")
    private String qqUrlRedirect;


    public String getProjectFolder() {
        return ProjectFolder;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public String[] getAdmin() {
        return admin;
    }

    public String getQqAppId() {
        return qqAppId;
    }

    public String getQqAppKey() {
        return qqAppKey;
    }

    public String getQqUrlAuthrization() {
        return qqUrlAuthrization;
    }

    public String getQqUrlAccessToken() {
        return qqUrlAccessToken;
    }

    public String getQqUrlOpenId() {
        return qqUrlOpenId;
    }

    public String getQqUrlUserInfo() {
        return qqUrlUserInfo;
    }

    public String getQqUrlRedirect() {
        return qqUrlRedirect;
    }
}
