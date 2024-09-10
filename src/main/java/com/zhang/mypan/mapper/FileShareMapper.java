package com.zhang.mypan.mapper;

import com.zhang.mypan.entity.FileShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author 86180
 * @description 针对表【file_share】的数据库操作Mapper
 * @createDate 2024-05-06 15:14:35
 * @Entity com.zhang.mypan.entity.FileShare
 */
public interface FileShareMapper extends BaseMapper<FileShare> {

    List<FileShare> loadShareList(String userId, String fileNameFuzzy, boolean showFileInfo);

    void updateByShareId(String shareId);

    FileShare selectByShareId(String shareId);

    List<FileShare> loadShareList1(String filePid, String userId, Integer pageNo, Integer pageSize);

}




