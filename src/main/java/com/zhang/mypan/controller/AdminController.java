package com.zhang.mypan.controller;


import com.zhang.mypan.annotation.GlobalIntercepter;
import com.zhang.mypan.annotation.VerifyParam;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.dto.SysSettingDto;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.query.FileInfoQuery;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.service.UserService;
import com.zhang.mypan.utils.RedisConstants;
import com.zhang.mypan.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private FileInfoService fileInfoService;
    private RedisUtil redisUtil;
    private UserService userService;

    /**
     * 获取系统设置
     *
     * @return
     */
    @RequestMapping("/getSysSettings")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result getSysSetting() {
        return Result.ok(redisUtil.get(RedisConstants.SYS_SETTING_KEY, SysSettingDto.class));
    }

    /**
     * 保存系统设置
     *
     * @param sysSettingDto
     * @return
     */
    @RequestMapping("/saveSettings")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result saveSetting(@VerifyParam(required = true) SysSettingDto sysSettingDto) {
        redisUtil.set(RedisConstants.SYS_SETTING_KEY, sysSettingDto);
        return Result.ok("保存设置成功");
    }

    /**
     * 获取用户列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/loadUserList")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result loadUserList(@VerifyParam(required = true) Integer pageNo,
                               @VerifyParam(required = true) Integer pageSize,
                               Integer status,
                               String nickNameFuzzy) {
        return userService.loadUserList(pageNo, pageSize, status, nickNameFuzzy);
    }

    /**
     * 更新用户状态
     *
     * @param userId
     * @param status
     * @return
     */
    @RequestMapping("/updateUserStatus")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result updateUserStatus(@VerifyParam(required = true) String userId,
                                   @VerifyParam(required = true) Integer status) {
        return userService.updateUserStatus(userId, status);
    }

    /**
     * 更新用户空间大小
     *
     * @param userId
     * @param changeSpace
     * @return
     */
    @RequestMapping("/updateUserSpace")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result updateUserSpace(@VerifyParam(required = true) String userId,
                                  @VerifyParam(required = true) Integer changeSpace) {
        userService.setUserSpace(userId, null, Long.valueOf(changeSpace));
        return Result.ok("更新成功");
    }


    /**
     * 删除用户所有的文件
     *
     * @param userId
     * @return
     */
    @RequestMapping("/deleteAllFiles")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result deleteAllFiles(@VerifyParam(required = true) String userId) {
        fileInfoService.deleteAllFiles(userId);
        userService.deleteUserSpace(userId);
        return Result.ok("已删除用户所有文件");
    }

    /**
     * 修改用户的空间大小
     *
     * @param userId      用户Id
     * @param changeSpace 用户空间偏移量
     * @return
     */
    @RequestMapping("/changeUserSpace")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result changeUserSpace(@VerifyParam(required = true) String userId,
                                  @VerifyParam(required = true) Long changeSpace) {
        userService.setUserSpace(userId, null, changeSpace);
        return Result.ok("修改成功");
    }

    /**
     * 获取所有用户文件
     *
     * @param query 参数
     * @return
     */
    @RequestMapping("/loadFileList")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result loadFileList(@VerifyParam(required = true) FileInfoQuery query) {
        return fileInfoService.loadFileList(query);
    }

    /**
     * 获取目录详情
     *
     * @param path
     * @return
     */
    @RequestMapping("/getFolderList")
    @GlobalIntercepter(checkAdmin = true, checkParam = true)
    public Result getFolderList(@VerifyParam(required = true) String path) {
        // 管理员获取所有的文件，无需用户id
        return fileInfoService.getFolderInfo(path, null);
    }

    /**
     * 获取文件信息
     *
     * @param userId
     * @param fileId
     * @param response
     * @return
     */
    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalIntercepter(checkAdmin = true, checkParam = true)
    public Result getFolderList(@PathVariable("userId") String userId,
                                @PathVariable("fileId") String fileId,
                                HttpServletResponse response) {
        // 管理员获取所有的文件，无需用户id
        return fileInfoService.getFile(response, fileId, userId);
    }

    /**
     * 获取视频图片文件
     *
     * @param userId
     * @param fileId
     * @param response
     * @return
     */
    @RequestMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalIntercepter(checkAdmin = true, checkParam = true)
    public Result getImage(@PathVariable("userId") String userId,
                           @PathVariable("fileId") String fileId,
                           HttpServletResponse response) {
        // 管理员获取所有的文件，无需用户id
        return fileInfoService.getFile(response, fileId, userId);
    }

    /**
     * 创建文件下载链接(下载码)
     *
     * @param fileId
     * @param userId
     * @return
     */
    @RequestMapping("createDownloadUrl/{userId}/{fileId}")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result createDownloadUrl(@PathVariable("fileId") String fileId,
                                    @PathVariable("userId") String userId) {
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
    public void download(@PathVariable("code") String code,
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
     * @param fileIdAndUserIds user1_file1,file2,file3&user2_file1,file2,file3
     * @return
     */
    @RequestMapping("delFile")
    @GlobalIntercepter(checkParam = true, checkAdmin = true)
    public Result delFile(@VerifyParam(required = true) String fileIdAndUserIds) {
        String[] arr = fileIdAndUserIds.split(",");

        for (String item : arr) {
            String[] fileIdsAndUserId = item.split("_");
            System.out.println("要删除的文件" + fileIdsAndUserId[0] + "&" + fileIdsAndUserId[1]);
            if (fileInfoService.delFileReal(fileIdsAndUserId[0], fileIdsAndUserId[1]) == null) {
                return Result.fail("删除失败", CodeEnum.CONFILCT);
            }
        }
        return Result.ok("已删除完毕");

    }


    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
