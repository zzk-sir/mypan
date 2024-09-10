package com.zhang.mypan.controller;


import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.service.FileShareService;
import com.zhang.mypan.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部分享
 */
@RestController
@RequestMapping("/share")
public class ShareController {
    private FileShareService fileShareService;

    /**
     * 加载分享列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("loadDataList")
    @GlobalIntercepter(checkParam = true)
    public Result loadShareList(@VerifyParam(required = true) Integer pageNo,
                                @VerifyParam(required = true) Integer pageSize,
                                String fileNameFuzzy) {
        final String userId = UserHolder.getUser().getId();
        String nameFuzzy = fileNameFuzzy == null ? "" : fileNameFuzzy;
        return fileShareService.loadShareList(userId, pageNo, pageSize, "%" + nameFuzzy + "%");
    }

    /**
     * 分享文件
     *
     * @param fileId    分享的文件ID
     * @param validType 分享时长
     * @return
     */
    @RequestMapping("shareFile")
    @GlobalIntercepter(checkParam = true)
    public Result shareFile(@VerifyParam(required = true) String fileId,
                            @VerifyParam(required = true) Integer validType,
                            String code) {
        final String userId = UserHolder.getUser().getId();
        return fileShareService.shareFile(userId, fileId, validType, code);
    }

    @RequestMapping("cancelShare")
    @GlobalIntercepter(checkParam = true)
    public Result cancelShare(@VerifyParam(required = true) String shareIds) {
        final String userId = UserHolder.getUser().getId();
        return fileShareService.cancelShare(userId, shareIds);
    }

    @Autowired
    public void setFileShareService(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }
}
