package com.zhang.mypan.utils;

/**
 * 系统常量类
 */
public final class SystemConstants {
    public static final int EMAIL_CODE_LEN = 6;  // 邮箱验证码长度
    public static final int PASSWORD_MIN_LEN = 6;  // 密码最小长度
    public static final int PASSWORD_MAX_LEN = 16;  // 密码最大长度

    public static final int IMG_CODE_LEN = 4; // 图片验证码长度
    public static final int USER_ID_LEN = 15; // 用户id长度
    public static final int USER_MAX_SPACE_MB = 1000; //用户最多默认使用空间 1000MB
    public static final int USER_IS_ENABLE = 0; // 用户状态可用
    public static final int USER_IS_DISABLE = 1; // 用户状态不可用

    public static final String FILE_FOLDER_FILE = "/file/"; // 文件存放路径
    public static final String FILE_FOLDER_AVATAR = "/file/avatar/"; // 用户头像存放路径
    public static final String FILE_TMP_FOLDER = "/file/tmp/"; // 临时文件存放路径
    public static final String FILE_FOLDER_AVATAR_DEFAULT = "/file/avatar/default/"; // 默认头像路径
    public static final int RANDOM_QQ_STATE_LEN = 30; // qq随机状态码长度

    public static final String VIEW_OBJ_RESULT_KEY = "result"; // qq回调url解析标志
    public static final int NICKNAME_MAX_LEN = 20; // 昵称最大长度

    public static final String MQ_MSG_QUEUE = "mypan.msg.queue"; // 消息队列
    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";
    public static final String IMAGE_PNG_SUFFIX = ".png";
    public static final Integer IMAGE_WIDTH = 150;  // 缩略图的宽度
    public static final String ROOT_FOLDER_ID = "0";
    public static final String MQ_DELFILE_QUEUE = "mypan.delFile.queue"; //  删除文件队列
    public static final int SHARE_ID_LEN = 20;
    public static final int SHARE_CODE_LEN = 5;
    public static final int RANDOM_FILE_ID_LEN = 10; // 随机文件ID长度

    public static final String REAL_DEL_FILE_MD5 = "-1"; // 实际文件删除后的md5值
}
