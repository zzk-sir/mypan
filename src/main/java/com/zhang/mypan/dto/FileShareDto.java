package com.zhang.mypan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhang.mypan.entity.FileShare;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class FileShareDto {
    private Integer folderType;

    private Integer fileCategory;

    private Integer fileType;
    private String fileName;

    private String fileCover;
    private String shareId;

    private Integer vaildType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shareTime;
    private Integer fileSize;

    private String code;

    private Integer showCount;

    public static FileShareDto parseFileShareDto(FileShare fileShare) {
        FileShareDto dto = new FileShareDto();
        if (fileShare != null) {
            dto.setFolderType(fileShare.getFolderType());
            dto.setFileCategory(fileShare.getFileCategory());
            dto.setFileType(fileShare.getFileType());
            dto.setFileName(fileShare.getFileName());
            dto.setFileCover(fileShare.getFileCover());
            dto.setShareId(fileShare.getShareId());
            dto.setVaildType(fileShare.getVaildType());
            dto.setExpireTime(fileShare.getExpireTime());
            dto.setShareTime(fileShare.getShareTime());
            dto.setCode(fileShare.getCode());
            dto.setShowCount(fileShare.getShowCount());
            dto.setFileSize(fileShare.getFileSize());
        }
        return dto;
    }
}
