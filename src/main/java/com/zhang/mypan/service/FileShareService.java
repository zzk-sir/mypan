package com.zhang.mypan.service;

import com.zhang.mypan.dto.Result;
import com.zhang.mypan.dto.UserDTO;
import com.zhang.mypan.entity.FileShare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.mypan.vo.ShareInfoVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * @author 86180
 * @description 针对表【file_share】的数据库操作Service
 * @createDate 2024-05-06 15:14:35
 */
public interface FileShareService extends IService<FileShare> {

    Result loadShareList(String userId, Integer pageNo, Integer pageSize, String fileNameFuzzy);


    Result shareFile(String userId, String fileId, Integer validType, String code);

    Result cancelShare(String userId, String shareIds);

    Result getShareInfo(String shareId);

    Result getShareLoginInfo(String shareId, UserDTO userDTO);

    Result checkShareCode(String shareId, String code);

    Result loadFileList(String shareId, String filePid, Integer pageNo, Integer pageSize);

    Result getFolderInfo(String path, String userId);

    ShareInfoVO checkShare(String shareId);

    Result getFile(HttpServletResponse response, String fileId, String userId);

    Result createDownloadUrl(String fileId, String userId);

    void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException;

    void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String currentUserId);

}
