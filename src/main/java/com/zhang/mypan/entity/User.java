package com.zhang.mypan.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @TableName user_info
 */
@TableName(value = "user_info")
@Data
public class User implements Serializable {
    @TableId
    private String userId;

    private String email;

    private String nickName;

    private String qqOpenId;

    private String qqAvatar;

    private String password;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Long useSpace;
    //    (value = "0",delval = "1")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    private Long totalSpace;

    private static final long serialVersionUID = 1L;

}