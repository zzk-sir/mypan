package com.zhang.mypan.controller;


import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("recycle")
public class RecycleController {
    private FileInfoService fileInfoService;

    /**
     * 获取回收栈列表
     *
     * @param pageNo   当前页
     * @param pageSize 每页大小
     * @return
     */
    @RequestMapping("loadRecycleList")
    @GlobalIntercepter(checkParam = true)
    public Result loadRecycleList(Integer pageNo, Integer pageSize) {
        final String userId = UserHolder.getUser().getId();
        return fileInfoService.loadRecycleList(userId, pageNo, pageSize);
    }

    /**
     * 恢复文件
     *
     * @param fileIds 恢复的文件ID
     * @return
     */
    @RequestMapping("recoverFile")
    @GlobalIntercepter(checkParam = true)
    public Result loadRecycleList(@VerifyParam(required = true) String fileIds) {
        final String userId = UserHolder.getUser().getId();
        return fileInfoService.recoverFile(userId, fileIds);
    }

    /**
     * 删除文件(真正删除)
     *
     * @param fileIds 要删除的文件Ids
     * @return
     */
    @RequestMapping("delFile")
    @GlobalIntercepter(checkParam = true)
    public Result delFile(@VerifyParam(required = true) String fileIds) {
        final String userId = UserHolder.getUser().getId();
        return fileInfoService.delFileReal(userId, fileIds);
    }

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }
}
