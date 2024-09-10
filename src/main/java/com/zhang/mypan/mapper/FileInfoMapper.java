package com.zhang.mypan.mapper;

import com.zhang.mypan.dto.FileDto;
import com.zhang.mypan.entity.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 86180
 * @description 针对表【file_info】的数据库操作Mapper
 * @createDate 2024-03-20 08:40:24
 * @Entity com.zhang.mypan.entity.FileInfo
 */
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    Long getUseSpaceByUserId(@Param("userId") String userId);

    void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
                                @Param("userId") String userId,
                                @Param("filePidList") List<String> filePidList,
                                @Param("fileIdList") List<String> fileIdList,
                                @Param("oldDelFlag") Integer oldDelFlag);

    @MapKey("file_name")
    List<FileInfo> selectSameNameFile(@Param("userId") String userId,
                                      @Param("fileIds") String[] fileIdArray,
                                      @Param("excludeDelFlag") Integer delFlag);

    Long getDelFileTotalSize(@Param("delFileIds") List<String> delFileList, @Param("userId") String userId);

    List<String> getAllfilesIdByUserId(String userId);

    List<FileDto> loadFileList(String filePid, String fileNameFuzzy);

    List<String> filterSecondUploadFile(List<String> delFileList);

    List<FileDto> loadFileList1(String filePid, String fileId, String userId);
}




