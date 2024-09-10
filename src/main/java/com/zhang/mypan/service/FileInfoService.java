package com.zhang.mypan.service;

import com.zhang.mypan.dto.Result;
import com.zhang.mypan.entity.FileInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.mypan.query.FileInfoQuery;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author 86180
 * @description 针对表【file_info】的数据库操作Service
 * @createDate 2024-03-20 08:40:24
 */
public interface FileInfoService extends IService<FileInfo> {

    Result loadDataList(FileInfoQuery query, String category);

    Result uploadFile(String fileId, MultipartFile file, String filename, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

    Long getUseSpace(String userId);

    Result getFile(HttpServletResponse response, String fileId, String userId);

    public void findAllSubFolderAndFile(List<String> delFileList, long[] totalSize, String userId, String fileId);

    Result newFolder(String filePid, String userId, String fileName);

    Result getFolderInfo(String path, String userId);

    Result rename(String fileId, String userId, String fileName);

    Result loadAllFolder(String filePid, String userId, String currentFileIds);

    Result changeFileFolder(String fileIds, String userId, String filePid);

    Result createDownloadUrl(String fileId, String userId);

    void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException;

    Result delFile(String userId, String fileIds);

    Result loadRecycleList(String userId, Integer pageNo, Integer pageSize);

    Result recoverFile(String userId, String fileIds);

    Result delFileReal(String userId, String fileIds);

    void delFileReal1(String userId, List<FileInfo> list);

    void deleteAllFiles(String userId);

    Result loadFileList(FileInfoQuery query);


    List<String> filterSecondUploadFile(List<String> delFileList);

    Result loadFileList1(FileInfoQuery query);
}
