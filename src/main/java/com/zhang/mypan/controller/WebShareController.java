package com.zhang.mypan.controller;

import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.dto.UserDTO;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.service.FileShareService;
import com.zhang.mypan.utils.UserHolder;
import com.zhang.mypan.vo.ShareInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * 外部分享
 */
@RestController
@RequestMapping("/showShare")
public class WebShareController {
    private FileShareService fileShareService;

    /**
     * 获取分享信息 (检测是否是自己分享自己查看的,获取用户登录信息)
     *
     * @param shareId
     * @return
     */
    @RequestMapping("/getShareLoginInfo")
    @GlobalIntercepter(checkParam = true)
    public Result getShareLoginInfo(@VerifyParam(required = true) String shareId) {
        final UserDTO user = UserHolder.getUser();
        return fileShareService.getShareLoginInfo(shareId, user);
    }


    /**
     * 获取分享信息
     *
     * @param shareId
     * @return
     */
    @RequestMapping("/showShare")
    @GlobalIntercepter(checkParam = true)
    public Result getShareInfo(@VerifyParam(required = true) String shareId) {
        final UserDTO user = UserHolder.getUser();
        return fileShareService.getShareInfo(shareId);
    }

    /**
     * 检测分享码
     *
     * @param shareId
     * @param code
     * @return
     */
    @RequestMapping("/checkShareCode")
    @GlobalIntercepter(checkParam = true)
    public Result checkShareCode(@VerifyParam(required = true) String shareId,
                                 @VerifyParam(required = true) String code) {
        return fileShareService.checkShareCode(shareId, code);
    }

    /**
     * 获取分享人分享的文件
     *
     * @param shareId 分享ID
     * @param filePid 如果是文件夹filePid = fileId 否则 为null
     * @return
     */
    @RequestMapping("/loadFileList")
    @GlobalIntercepter(checkParam = true)
    public Result loadFileList(@VerifyParam(required = true) String shareId,
                               @VerifyParam(required = true) String filePid,
                               @VerifyParam(required = true) Integer pageNo,
                               @VerifyParam(required = true) Integer pageSize) {
        return fileShareService.loadFileList(shareId, filePid, pageNo, pageSize);
    }

    /**
     * 获取目录详情
     *
     * @param path 路径
     * @return
     */
    @RequestMapping("getFolderInfo")
    @GlobalIntercepter(checkParam = true)
    public Result getFolderInfo(@VerifyParam(required = true) String path,
                                @VerifyParam(required = true) String shareId) {
        final ShareInfoVO shareInfoVO = fileShareService.checkShare(shareId);
        if (shareInfoVO == null) {
            return Result.fail(CodeEnum.ISEXPIRED);
        }
        return fileShareService.getFolderInfo(path, shareInfoVO.getUserId());
    }

    /**
     * 获取文件信息
     *
     * @param shareId
     * @param fileId
     * @param response
     * @return
     */
    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalIntercepter(checkAdmin = true)
    public Result getFolderList(@PathVariable("shareId") String shareId,
                                @PathVariable("fileId") String fileId,
                                HttpServletResponse response) {
        final ShareInfoVO shareInfoVO = fileShareService.checkShare(shareId);
        if (shareInfoVO == null) {
            return Result.fail(CodeEnum.ISEXPIRED);
        }
        return fileShareService.getFile(response, fileId, shareInfoVO.getUserId());
    }

    /**
     * 获取视频图片文件
     *
     * @param shareId
     * @param fileId
     * @param response
     * @return
     */
    @RequestMapping("/ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalIntercepter(checkParam = true)
    public Result getImage(@PathVariable("shareId") String shareId,
                           @PathVariable("fileId") String fileId,
                           HttpServletResponse response) {
        final ShareInfoVO shareInfoVO = fileShareService.checkShare(shareId);
        if (shareInfoVO == null) {
            return Result.fail(CodeEnum.ISEXPIRED);
        }
        return fileShareService.getFile(response, fileId, shareInfoVO.getUserId());
    }

    /**
     * 创建文件下载链接(下载码)
     *
     * @param fileId
     * @param shareId
     * @return
     */
    @RequestMapping("createDownloadUrl/{shareId}/{fileId}")
    @GlobalIntercepter(checkParam = true)
    public Result createDownloadUrl(@PathVariable("fileId") String fileId,
                                    @PathVariable("shareId") String shareId) {
        final ShareInfoVO shareInfoVO = fileShareService.checkShare(shareId);
        if (shareInfoVO == null) {
            return Result.fail(CodeEnum.ISEXPIRED);
        }
        return fileShareService.createDownloadUrl(fileId, shareInfoVO.getUserId());
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
    public void download(@PathVariable("code") String code,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        try {
            fileShareService.download(request, response, code);
        } catch (UnsupportedEncodingException e) {
            throw new MyPanException(CodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    /**
     * 保存到我的文件夹下 ,
     *
     * @param shareId
     * @param shareFileIds
     * @param myFolderId
     */
    @RequestMapping("saveShare")
    @GlobalIntercepter(checkParam = true)
    public Result saveShare(@VerifyParam(required = true) String shareId,
                            @VerifyParam(required = true) String shareFileIds,
                            @VerifyParam(required = true) String myFolderId) {
        final ShareInfoVO shareInfoVO = fileShareService.checkShare(shareId);
        if (shareInfoVO == null) {
            return Result.fail(CodeEnum.ISEXPIRED);
        }
        UserDTO userDto = UserHolder.getUser();
        if (shareInfoVO.getUserId().equals(userDto.getId())) {
            return Result.fail("自己不能保存自己分享的文件", CodeEnum.BAD_REQUEST);
        }
        //验证用户空间
        fileShareService.saveShare(shareInfoVO.getFileId(), shareFileIds, myFolderId, shareInfoVO.getUserId(), userDto.getId());
        return Result.ok();
    }

    @Autowired
    public void setFileShareService(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }
}
