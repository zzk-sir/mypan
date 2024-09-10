package com.zhang.mypan.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileInfoQuery extends BaseParam {
    /**
     * 文件id
     */
    private String fileId;
    private String fileIdFuzzy;
    /**
     * 用户id
     */
    private String userId;
    private String userIdFuzzy;
    /**
     * fileMd5
     */
    private String fileMd5;
    private String fileMd5Fuzzy;
    /**
     * 文件的父级id
     */
    private String filePid;
    private String filePidFuzzy;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件名
     */
    private String fileName;
    private String fileNameFuzzy;
    /**
     * 文件封面
     */
    private String fileCovery;
    private String fileCoveryFuzzy;
    /**
     * 文件路径
     */
    private String filePath;
    private String filePathFuzzy;
    /**
     * 文件创建时间
     */
    private String createTime;
    private String createTimeStart;
    /**
     * 文件排序方式
     */
    private String orderBy;
    /**
     * 文件删除标识 0未删除 1回收站 2 正常
     */
    private Integer delFlag;

    private Integer fileCategory;
    /**
     * 当前页
     */
    private Integer pageNo = 0;
    /**
     * 每页大小
     */
    private Integer pageSize = 15;

    private Integer status;

    private Integer folderType;

    private String[] fileIdArray;

    private String[] excludeFileIdArray;


}
