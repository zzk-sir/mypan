package com.zhang.mypan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhang.mypan.entity.FileInfo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class FileDto {
    private String userId;
    private String fileId;
    private String filePid;
    private Long fileSize;
    private String fileName;
    private String fileCover;
    private String nickName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recoveryTime;
    private Integer folderType;
    private Integer fileCategory;
    private Integer fileType;
    private Integer delFlag;
    private Integer status;

    public static FileDto parseFileDto(FileInfo fileInfo) {
        System.out.println("fileInfo" + fileInfo);
        FileDto fileDto = new FileDto();
        if (fileInfo != null) {
            fileDto.setFileId(fileInfo.getFileId());
            fileDto.setFilePid(fileInfo.getFilePid());
            fileDto.setFileSize(fileInfo.getFileSize());
            fileDto.setFileName(fileInfo.getFileName());
            fileDto.setFileCover(fileInfo.getFileCover());
            fileDto.setCreateTime(fileInfo.getCreateTime());
            fileDto.setLastUpdateTime(fileInfo.getLastUpdateTime());
            fileDto.setFileCategory(fileInfo.getFileCategory());
            fileDto.setFileType(fileInfo.getFileType());
            fileDto.setStatus(fileInfo.getStatus());
            fileDto.setFolderType(fileInfo.getFolderType());
            fileDto.setRecoveryTime(fileInfo.getRecoveryTime());
        }
        System.out.println("fileDTO" + fileDto);
        return fileDto;
    }
}
