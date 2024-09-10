package com.zhang.mypan.utils;

/**
 * redis 常量类
 */
public final class RedisConstants {
    public static final long EMAIL_CODE_TTL = 15 * 60;// 15分钟
    public static final String EMAIL_SETTING_KEY = "mypan:emailsetting:";// 邮箱
    public static final String EMAIL_CODE_KEY = "mypan:emailcode:";// 邮箱验证码

    public static final String USER_LOGIN_KEY = "mypan:user:login:"; // 用户登录信息
    public static final long USER_LOGIN_TTL = 60 * 60; // 用户登录1小时

    public static final String USER_FILE_TEMP_SIZE = "mypan:user:file:tmp"; // 存储用户上传文件的大小

    public static final long FILE_TEMP_EXPIRE_TTL = 60 * 60 * 24 * 3; //  设置文件上传的过期时间 3天，超过三天不能续传
    public static final int DOWNLOAD_RANDOM_STR_LEN = 50; // 下载码的长度
    public static final String DOWNLOAD_CODE_KEY = "mypan:downloadcode:"; // 下载码前缀
    public static final long DOWNLOAD_CODE_EXPIRE_TIME = 5 * 60; // 下载码的有效时间 5 分钟
    public static final String DOWNLOAD_FILEID_MAP = "mypan:download:"; // 下载码映射 防止同时请求，redis添加多个占用内存
    public static final String SYS_SETTING_KEY = "mypan:syssetting:"; //系统设置

    public static final String SHARE_CODE_KEY = "mypan:sharecode:"; // 分享文件信息
}
