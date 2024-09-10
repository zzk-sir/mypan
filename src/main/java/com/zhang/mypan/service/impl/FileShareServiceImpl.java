package com.zhang.mypan.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.mypan.dto.FileShareDto;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.dto.UserDTO;
import com.zhang.mypan.entity.FileInfo;
import com.zhang.mypan.entity.FileShare;
import com.zhang.mypan.entity.User;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.enums.FileDelFlagEnums;
import com.zhang.mypan.enums.FileFolderTypeEnums;
import com.zhang.mypan.enums.ShareVaildTypeEnums;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.query.FileInfoQuery;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.service.FileShareService;
import com.zhang.mypan.mapper.FileShareMapper;
import com.zhang.mypan.service.UserService;
import com.zhang.mypan.utils.*;
import com.zhang.mypan.vo.PaginationResultVO;
import com.zhang.mypan.vo.ShareInfoVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 86180
 * @description 针对表【file_share】的数据库操作Service实现
 * @createDate 2024-05-06 15:14:35
 */
@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare>
        implements FileShareService {
    private FileInfoService fileInfoService;
    ;
    private UserService userService;
    private RedisUtil redisUtil;

    @Override
    public Result loadShareList(String userId, Integer pageNo, Integer pageSize, String fileNameFuzzy) {
        System.out.println(fileNameFuzzy);
        List<FileShareDto> shares = this.baseMapper
                .loadShareList(userId, fileNameFuzzy, true)
                .stream().map(FileShareDto::parseFileShareDto).collect(Collectors.toList());
        // 分页
        final Page<FileShareDto> page = PageUtil.page(shares, pageNo, pageSize);

        return Result.ok(new PaginationResultVO<>(page.getTotal(), page.getSize(), page.getCurrent(), page.getPages(), page.getRecords()));
    }

    @Override
    public Result shareFile(String userId, String fileId, Integer validType, String code) {
        FileShare fileShare = new FileShare();
        fileShare.setShareId(RandomUtil.randomString(SystemConstants.SHARE_ID_LEN));
        fileShare.setUserId(userId);
        fileShare.setFileId(fileId);
        final ShareVaildTypeEnums byCode = ShareVaildTypeEnums.getByCode(validType);
        if (byCode == null) throw new MyPanException(CodeEnum.BAD_REQUEST);
        fileShare.setVaildType(byCode.getType());
        fileShare.setShareTime(new Date());
        if (!byCode.equals(ShareVaildTypeEnums.FOREVER)) {
            fileShare.setExpireTime(DateUtil.addDay(byCode.getDays()));
        }
        if (code == null) {
            fileShare.setCode(RandomUtil.randomString(SystemConstants.SHARE_CODE_LEN));
        } else {
            fileShare.setCode(code);
        }
        fileShare.setShowCount(0);
        save(fileShare);
        return Result.ok(fileShare);
    }

    @Override
    public Result cancelShare(String userId, String shareIds) {
        String[] shareIdArray = shareIds.split(",");
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getUserId, userId)
                .in(FileShare::getShareId, Arrays.asList(shareIdArray));
        remove(wrapper);
        return Result.ok("取消成功");
    }

    @Override
    public Result getShareInfo(String shareId) {
        FileShare share = baseMapper.selectByShareId(shareId);
        // 不存在，失效
        if (null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            return Result.fail("分享链接不存在或已失效", CodeEnum.ISEXPIRED);
        }
        ShareInfoVO shareInfoVO = new ShareInfoVO(share);
        return getResult(shareInfoVO);
    }

    @NotNull
    private Result getResult(ShareInfoVO shareInfoVO) {
        FileInfo fileInfo = fileInfoService.lambdaQuery()
                .eq(FileInfo::getFileId, shareInfoVO.getFileId())
                .eq(FileInfo::getUserId, shareInfoVO.getUserId()).one();
        // 不存在或者已删除
        if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
            return Result.fail("文件不存在或者已删除", CodeEnum.ISEXPIRED);
        }
        shareInfoVO.setFileName(fileInfo.getFileName());
        User user = userService.lambdaQuery().eq(User::getUserId, shareInfoVO.getUserId()).one();
        shareInfoVO.setNickName(user.getNickName());
        shareInfoVO.setAvatar(user.getQqAvatar());
        return Result.ok(shareInfoVO);
    }

    @Override
    public Result getShareLoginInfo(String shareId, UserDTO userDTO) {
        FileShare share = baseMapper.selectByShareId(shareId);
        // 不存在，失效
        if (null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            return Result.fail("分享链接不存在或已失效", CodeEnum.ISEXPIRED);
        }
        ShareInfoVO shareInfoVO = new ShareInfoVO(share);
        if (userDTO != null && userDTO.getId().equals(share.getUserId())) {
            shareInfoVO.setCurrentUser(true);
        }
        return getResult(shareInfoVO);
    }

    @Override
    public Result checkShareCode(String shareId, String code) {
        FileShare share = baseMapper.selectByShareId(shareId);
        // 不存在，失效
        if (null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            return Result.fail("分享链接不存在或已失效", CodeEnum.ISEXPIRED);
        }
        // 检测code 是否正确
        if (!share.getCode().equals(code)) {
            return Result.fail("验证码错误", CodeEnum.BAD_REQUEST);
        }
        // 更新浏览次数
        this.baseMapper.updateByShareId(shareId);
        final ShareInfoVO shareInfoVO = new ShareInfoVO((share));
        // 存到redis中
        redisUtil.hmsetFromObj(RedisConstants.SHARE_CODE_KEY + shareId, shareInfoVO);
        return Result.ok(shareInfoVO);
    }

    @Override
    public Result loadFileList(String shareId, String filePid, Integer pageNo, Integer pageSize) {
        final ShareInfoVO shareInfoVO = redisUtil.hmgetToObj(RedisConstants.SHARE_CODE_KEY + shareId, new ShareInfoVO());
        if (null == shareInfoVO) {
            return Result.fail("分享验证失败，请重新输入验证码", CodeEnum.CHECKFAIL);
        }
        if (shareInfoVO.getExpireTime() != null && new Date().after(shareInfoVO.getExpireTime())) {
            return Result.fail("分享链接不存在或已失效", CodeEnum.ISEXPIRED);
        }
        // 查找文件类型
        FileInfoQuery query = new FileInfoQuery();
        if ("0".equals(filePid)) {
            query.setFileId(shareInfoVO.getFileId());
        } else {
            query.setFilePid(filePid);
        }
        query.setUserId(shareInfoVO.getUserId());
        query.setPageNo(pageNo - 1);
        query.setPageSize(pageSize);
        return fileInfoService.loadFileList1(query);

    }


    @Override
    public Result getFolderInfo(String path, String userId) {
        return fileInfoService.getFolderInfo(path, userId);
    }

    @Override
    public ShareInfoVO checkShare(String shareId) {
        return redisUtil.hmgetToObj(RedisConstants.SHARE_CODE_KEY + shareId, new ShareInfoVO());
    }

    @Override
    public Result getFile(HttpServletResponse response, String fileId, String userId) {
        return fileInfoService.getFile(response, fileId, userId);
    }

    @Override
    public Result createDownloadUrl(String fileId, String userId) {
        return fileInfoService.createDownloadUrl(fileId, userId);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        fileInfoService.download(request, response, code);
    }

    @Override
    public void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String currentUserId) {
        String[] shareFileIdArray = shareFileIds.split(",");
        // 目标文件夹
        List<FileInfo> list = fileInfoService.lambdaQuery()
                .eq(FileInfo::getFilePid, myFolderId)
                .eq(FileInfo::getUserId, currentUserId).list();

        Map<String, FileInfo> currentFileMap = list.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) -> data2));

        // 选择的文件
        final List<FileInfo> list1 = fileInfoService.lambdaQuery().eq(FileInfo::getUserId, shareUserId)
                .in(FileInfo::getFileId, Arrays.asList(shareFileIdArray)).list();
        // 重命名选择的文件
        List<FileInfo> copyFileList = new ArrayList<>();
        Date curDate = new Date();
        long[] totalSize = new long[]{0};
        for (FileInfo item : list1) {
            FileInfo haveFile = currentFileMap.get(item.getFileName());
            // 相同文件名要重命名
            if (haveFile != null) {
                item.setFileName(item.getFileName() + UUID.randomUUID());
            }
            // 递归查找所有的子文件
            findAllSubFile(copyFileList, totalSize, item, shareUserId, currentUserId, curDate, myFolderId);
        }
        // 计算用户空间
        userService.setUserSpace(currentUserId, totalSize[0], null);

        // 将这些文件保存在数据库中
        fileInfoService.saveBatch(copyFileList);
    }

    private void findAllSubFile(List<FileInfo> copyFileList, long[] totalSize, FileInfo fileInfo, String sourceUserid, String currentUserId, Date curDate, String newFilePid) {
        String sourceFileId = fileInfo.getFileId();
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setFilePid(newFilePid);
        fileInfo.setUserId(currentUserId);
        String newFileId = RandomUtil.randomString(SystemConstants.RANDOM_FILE_ID_LEN);
        fileInfo.setFileId(newFileId);
        copyFileList.add(fileInfo);
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            List<FileInfo> sourceFileList = fileInfoService.lambdaQuery()
                    .eq(FileInfo::getFilePid, sourceFileId)
                    .eq(FileInfo::getUserId, sourceUserid)
                    .list();
            for (FileInfo item : sourceFileList) {
                findAllSubFile(copyFileList, totalSize, item, sourceUserid, currentUserId, curDate, newFileId);
            }
        } else {
            totalSize[0] += fileInfo.getFileSize();
        }

    }

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}




