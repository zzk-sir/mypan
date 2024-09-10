package com.zhang.mypan.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @TableName file_share
 */
@TableName(value = "file_share")
@Data
public class FileShare implements Serializable {
    private Integer folderType;
    private String nickName;
    private Integer fileCategory;
    private Date lastUpdateTime;
    private Integer fileType;
    private String fileName;

    private String fileCover;
    private String shareId;

    private String fileId;

    private String userId;

    private Integer vaildType;

    private Date expireTime;

    private Date shareTime;

    private String code;

    private Integer showCount;
    private Integer fileSize;

    private static final long serialVersionUID = 1L;
}