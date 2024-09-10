package com.zhang.mypan.controller;

import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.config.AppConfig;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.enums.RegexEnum;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.query.FileInfoQuery;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.utils.FileUtil;
import com.zhang.mypan.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/file")
public class FileController {
    private FileInfoService fileInfoService;
    private AppConfig appConfig;

    /**
     * 获取文件列表
     *
     * @param query    前端传入参数
     * @param category 类别
     * @return
     */
    @PostMapping("loadDataList")
    public Result loadDataList(FileInfoQuery query, String category) {
        return fileInfoService.loadDataList(query, category);
    }

    /**
     * 上传文件
     *
     * @param fileId     文件id
     * @param file       文件流
     * @param filePid    文件Pid
     * @param fileMd5    文件md5
     * @param chunkIndex 当前文件分片索引
     * @param chunks     文件分片总数
     * @return Result
     */
    @PostMapping("/uploadFile")
    @GlobalIntercepter(checkParam = true)
    public Result uploadFile(String fileId,
                             MultipartFile file,
                             @VerifyParam(required = true, regex = RegexEnum.VAILD_FILENAME) String fileName,
                             @VerifyParam(required = true) String filePid,
                             @VerifyParam(required = true) String fileMd5,
                             @VerifyParam(required = true) Integer chunkIndex,
                             @VerifyParam(required = true) Integer chunks) {
        return fileInfoService.uploadFile(fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);

    }

    /**
     * 获取图片
     *
     * @param response
     * @param imageFolder
     * @param imageName
     */
    @GetMapping("getImage/{imageFolder}/{imageName}")
    @GlobalIntercepter(checkParam = true)
    public void getImage(HttpServletResponse response,
                         @PathVariable("imageFolder") String imageFolder,
                         @PathVariable("imageName") String imageName) {
        FileUtil.getImage(response, imageFolder, imageName, appConfig);
    }


    @GetMapping("ts/getVideoInfo/{fileId}")
    @GlobalIntercepter(checkParam = true)
    public Result getVideo(HttpServletResponse response,
                           @PathVariable("fileId") String fileId) {
        final String userId = UserHolder.getUser().getId();
        return fileInfoService.getFile(response, fileId, userId);
    }

    @RequestMapping("getFile/{fileId}")
    @GlobalIntercepter(checkParam = true)
    public Result getFile(HttpServletResponse response,
                          @PathVariable("fileId") String fileId) {
        final String userId = UserHolder.getUser().getId();
        return fileInfoService.getFile(response, fileId, userId);
    }

    /**
     * 新建文件夹
     *
     * @param filePid  父文件ID
     * @param fileName 新建的文件名
     * @return
     */
    @RequestMapping("newFolder")
    @GlobalIntercepter(checkParam = true)
    public Result newFolder(@VerifyParam(required = true) String filePid,
                            @VerifyParam(required = true) String fileName) {
        final String userId = UserHolder.getUser().getId();
        return fileInfoService.newFolder(filePid, userId, fileName);
    }

    /**
     * 获取目录详情
     *
     * @param path 路径
     * @return
     */
    @RequestMapping("getFolderInfo")
    @GlobalIntercepter(checkParam = true)
    public Result getFolderInfo(@VerifyParam(required = true) String path) {
        String userId = UserHolder.getUser().getId();
        return fileInfoService.getFolderInfo(path, userId);
    }

    /**
     * 重命名
     *
     * @param fileId   文件ID
     * @param fileName 文件的新名字
     * @return
     */
    @RequestMapping("rename")
    @GlobalIntercepter(checkParam = true)
    public Result rename(@VerifyParam(required = true) String fileId,
                         @VerifyParam(required = true) String fileName) {
        String userId = UserHolder.getUser().getId();
        return fileInfoService.rename(fileId, userId, fileName);
    }

    /**
     * 加载文件
     *
     * @param filePid        文件父ID
     * @param currentFileIds 要排除的文件夹ID （自己,以及其所有子文件夹）
     * @return
     */
    @RequestMapping("loadAllFolder")
    @GlobalIntercepter(checkParam = true)
    public Result loadAllFolder(@VerifyParam(required = true) String filePid,
                                String currentFileIds) {
        String userId = UserHolder.getUser().getId();
        return fileInfoService.loadAllFolder(filePid, userId, currentFileIds);
    }

    /**
     * 移动文件 (这里会检测是否移动到了父文件夹下)
     *
     * @param fileIds 要移动的所有文件ID
     * @param filePid 当前文件的父ID
     * @return
     */
    @RequestMapping("changeFileFolder")
    @GlobalIntercepter(checkParam = true)
    public Result changeFileFolder(@VerifyParam(required = true) String fileIds,
                                   @VerifyParam(required = true) String filePid) {
        String userId = UserHolder.getUser().getId();
        return fileInfoService.changeFileFolder(fileIds, userId, filePid);
    }

    /**
     * 创建文件下载链接(下载码)
     *
     * @param fileId 下载的文件ID
     * @return
     */
    @RequestMapping("createDownloadUrl/{fileId}")
    @GlobalIntercepter(checkParam = true)
    public Result createDownloadUrl(@VerifyParam(required = true) @PathVariable("fileId") String fileId) {
        String userId = UserHolder.getUser().getId();
        return fileInfoService.createDownloadUrl(fileId, userId);
    }

    /**
     * 下载文件
     *
     * @param code     下载码
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("download/{code}")
    @GlobalIntercepter(checkParam = true)
    public void download(@VerifyParam(required = true) @PathVariable("code") String code,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        try {
            fileInfoService.download(request, response, code);
        } catch (UnsupportedEncodingException e) {
            throw new MyPanException(CodeEnum.FILE_DOWNLOAD_ERROR);

        }
    }

    /**
     * 删除文件(放入回收站)
     *
     * @param fileIds 删除的所有文件IDs
     */
    @RequestMapping("delFile")
    @GlobalIntercepter(checkParam = true)
    public Result delFile(@VerifyParam(required = true) String fileIds) {
        final String userId = UserHolder.getUser().getId();
        Result result = fileInfoService.delFile(userId, fileIds);
        return result != null ? result : Result.fail("回收文件失败", CodeEnum.NOT_FOUND);
    }


    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;

    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
}
