package com.zhang.mypan.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @TableName file_info
 */
@TableName(value = "file_info")
@Data
public class FileInfo implements Serializable {
    private String fileId;

    private String userId;

    private String fileMd5;

    private String filePid;

    private Long fileSize;

    private String fileName;

    private String fileCover;

    private String filePath;

    private Date createTime;

    private Date lastUpdateTime;

    private Integer folderType;

    private Integer fileCategory;

    private Integer fileType;

    private Integer status;

    private Date recoveryTime;

    private Integer delFlag;

    private static final long serialVersionUID = 1L;

}