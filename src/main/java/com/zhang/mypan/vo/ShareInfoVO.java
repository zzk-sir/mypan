package com.zhang.mypan.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhang.mypan.entity.FileShare;
import lombok.Data;

import java.util.Date;

@Data
public class ShareInfoVO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    private String nickName;
    private String fileName;
    private Boolean currentUser; // 是不是当前用户
    private String fileId;
    private String avatar;
    private String userId;

    public ShareInfoVO() {
    }

    public ShareInfoVO(FileShare fileShare) {
        currentUser = false;
        shareTime = fileShare.getShareTime();
        expireTime = fileShare.getExpireTime();
        fileName = fileShare.getFileName();
        fileId = fileShare.getFileId();
        userId = fileShare.getUserId();
        nickName = fileShare.getNickName();
    }
}
