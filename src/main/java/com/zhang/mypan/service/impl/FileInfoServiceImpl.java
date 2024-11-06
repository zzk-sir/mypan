package com.zhang.mypan.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.mypan.config.AppConfig;
import com.zhang.mypan.dto.*;
import com.zhang.mypan.entity.FileInfo;
import com.zhang.mypan.enums.*;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.query.FileInfoQuery;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.mapper.FileInfoMapper;
import com.zhang.mypan.service.UserService;
import com.zhang.mypan.utils.*;
import com.zhang.mypan.vo.PaginationResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.aspectj.util.FileUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type File info service.
 *
 * @author 86180
 * @description 针对表 【file_info】的数据库操作Service实现
 * @createDate 2024 -03-20 08:40:24
 */
@Service
@Slf4j
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
        implements FileInfoService {
    private UserService userService;
    private AppConfig appConfig;

    private RedisUtil redisUtil;

    private FileInfoServiceImpl fileInfoService;
    private RabbitTemplate rabbitTemplate;
    private static final String mqDelFileQueue = SystemConstants.MQ_DELFILE_QUEUE;

    @Override
    public Result loadDataList(FileInfoQuery query, String category) {
        // 获取类型枚举
        FileCategoryEnums categoryEnums = FileCategoryEnums.getByCode(category);
        // 找到了 设置响应的文件类型 0 1...
        if (categoryEnums != null) {
            query.setFileCategory(categoryEnums.getCategory());
        }
        // 从ThreadLocal中获取userId
        String userId = UserHolder.getUser().getId();
        // 设置userid
        query.setUserId(userId);
        // 设置排序方式
        query.setOrderBy("last_update_time desc");
        // 设置文件删除状态(使用中)
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        // 根据分页获取数据
        PaginationResultVO<FileDto> result = pagelist(query);
        return Result.ok(result);
    }

    // 前端分好片，逐个发送到后端
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result uploadFile(String fileId, MultipartFile file, String filename, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
        boolean uploadSuccess = true;
        File tempFileFolder = null;
        UploadResultDto resultDto = null;
        try {
            resultDto = new UploadResultDto();
            // 获取当前用户
            UserDTO userDTO = UserHolder.getUser();
            // 检测fileId是否存在
            if (StringUtils.isEmpty(fileId)) {
                //  不存在，生成随机
                fileId = RandomUtil.randomString(SystemConstants.RANDOM_FILE_ID_LEN);
            }
            resultDto.setFileId(fileId);
            Date curDate = new Date();
            // 检测用户空间是否充足（上传之前判断）
            if (chunkIndex == 0) {
                // 第一个分片
                // 在数据库中查找是否存在这个MD5
                List<FileInfo> fileInfos = query().eq("file_md5", fileMd5)
                        .eq("status", FileStatusEnums.USING.getStatus())
                        .list();
                boolean isExistInCommonPFolderAndName = fileInfos.stream().anyMatch(fileInfo ->
                        fileInfo.getFilePid().equals(filePid) && fileInfo.getFileName().equals(filename));
                FileInfo fileInfo = null;
                if (fileInfos.size() > 0) {
                    fileInfo = fileInfos.get(0);
                }
                if (fileInfo != null && !Objects.equals(fileInfo.getDelFlag(), FileDelFlagEnums.DEL.getFlag())) {
                    // 存在 秒传==========
                    // 判断文件大小
                    final Long fileSize = fileInfo.getFileSize();
                    long newUseSpace = fileSize + userDTO.getUserUseSpace();
                    if (newUseSpace > userDTO.getUserTotalSpace()) {
                        // 超出了用户网盘空间
                        log.error("要上传的文件大小:" + fileSize +
                                "\n当前用户使用空间的大小:" + userDTO.getUserTotalSpace() +
                                "\n当前用户的总空间大小:" + userDTO.getUserTotalSpace());
                        throw new MyPanException(CodeEnum.SPACE_OVERFLOW);
                    }
                    // 添加文件到数据库
                    fileInfo.setFileId(fileId);
                    fileInfo.setFilePid(filePid);
                    fileInfo.setUserId(userDTO.getId());
                    fileInfo.setCreateTime(curDate);
                    fileInfo.setLastUpdateTime(curDate);
                    fileInfo.setStatus(FileStatusEnums.USING.getStatus());
                    fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    fileInfo.setFileMd5(fileMd5);
                    // 文件重命名
                    if (isExistInCommonPFolderAndName) {
                        // 同一父文件夹下&&文件名相同
                        fileInfo.setFileName(UUID.randomUUID() + filename);
                    } else {
                        fileInfo.setFileName(filename);
                    }
                    // 将文件上传到数据库
                    save(fileInfo);
                    resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
                    // 更新用户使用空间(更改数据库，UserHolder，redis)
                    userService.setUserSpace(userDTO.getId(), fileSize, null);
                    // 返回给前端
                    return Result.ok(resultDto);
                }
            }
            //  判断磁盘空间
            Long currentSize = redisUtil.getFileTempSize(userDTO.getId(), fileId);

            if (file.getSize() + currentSize + userDTO.getUserUseSpace() > userDTO.getUserTotalSpace()) {
                //  磁盘空间不足
                throw new MyPanException(CodeEnum.SPACE_OVERFLOW);
            }

            // 暂存临时目录
            String tmpPath = appConfig.getProjectFolder() + SystemConstants.FILE_TMP_FOLDER;
            // userid fileid
            String currentUserFolderName = userDTO.getId() + fileId;

            tempFileFolder = new File(tmpPath + currentUserFolderName);
            if (!tempFileFolder.exists()) {
                // 不存在 创建
                tempFileFolder.mkdirs();
            }
            // 上传文件
            File newFile = new File(tempFileFolder.getPath() + File.separator + chunkIndex);
            file.transferTo(newFile);
            // 保存临时文件大小
            redisUtil.saveFileTempSize(userDTO.getId(), fileId, file.getSize());
            if (chunkIndex < chunks - 1) {
                resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
            } else {
                // 最后一个分片
                resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
                // 异步合并，添加到数据库
                // 月份
                String month = new SimpleDateFormat("yyyyMM").format(new Date());
                // 文件名 useridfileid.suffix
                String fileSuffix = filename.substring(filename.lastIndexOf("."));
                String realFileName = currentUserFolderName + fileSuffix;
                FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
                // 自动重命名
                // 获取上传文件的总大小
                Long totalSize = redisUtil.getFileTempSize(userDTO.getId(), fileId);
                if (totalSize == 0L) {
                    // 超时
                    throw new MyPanException(CodeEnum.FILE_TIMEOUT);
                }
                // 存入数据库
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileId(fileId);
                fileInfo.setUserId(userDTO.getId());
                fileInfo.setFileMd5(fileMd5);
                fileInfo.setFileName(filename);
                fileInfo.setFilePath(month + File.separator + realFileName);
                fileInfo.setFilePid(filePid);
                fileInfo.setCreateTime(curDate);
                fileInfo.setFileSize(totalSize);
                fileInfo.setLastUpdateTime(curDate);
                fileInfo.setFileCategory(fileTypeEnums.getCategory().getCategory());
                fileInfo.setFileType(fileTypeEnums.getType());
                fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
                fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
                fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
                //
                log.info("上传的文件信息：\n" +
                        "文件ID：" + fileId +
                        "\n用户ID：" + fileInfo.getUserId() +
                        "\n文件MD5：" + fileInfo.getFileMd5() +
                        "\n文件名：" + fileInfo.getFileName() +
                        "\n文件路径：" + fileInfo.getFilePath() +
                        "\n文件大小：" + fileInfo.getFileSize() +
                        "\n文件类型：" + fileInfo.getFileType() +
                        "\n文件分类：" + fileInfo.getFileCategory());
                save(fileInfo);
                // 更新用户使用空间大小
                userService.setUserSpace(userDTO.getId(), totalSize, null);
                // 设置事务处理完，执行转码
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 文件转码(使用spring管理，异步生效，类似于事务失效)
                        fileInfoService.asyncTransferFile(fileInfo.getFileId(), userDTO);
                    }
                });
            }
            return Result.ok(resultDto);
        } catch (MyPanException e) {
            uploadSuccess = false;
            log.error("文件上传失败", e);
            throw e;
        } catch (Exception e) {
            uploadSuccess = false;
            log.error("文件上传失败", e);
        } finally {
            if (!uploadSuccess && tempFileFolder != null) {
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    log.error("删除临时文件夹失败", e);

                }
            }
        }
        return Result.ok(resultDto);
    }

    /**
     * 获取用户使用空间大小
     *
     * @param userId
     * @return
     */
    @Override
    public Long getUseSpace(String userId) {
        return getBaseMapper().getUseSpaceByUserId(userId);
    }


    /**
     * 获取文件
     *
     * @param response
     * @param fileId
     * @param userId
     * @return
     */
    @Override
    public Result getFile(HttpServletResponse response, String fileId, String userId) {

        String filePath = null;

        if (fileId.endsWith(".ts")) {
            // 请求的是分片的视频文件
            // 这里的fileId是ts的文件名,要做响应的处理，提取 realFileid
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];
            FileInfo fileInfo = fileInfoService
                    .lambdaQuery()
                    .eq(FileInfo::getFileId, realFileId)
                    .eq(FileInfo::getUserId, userId)
                    .one();
            String fileNameNoSuffix = fileInfo.getFilePath().substring(0, fileInfo.getFilePath().lastIndexOf("."));
            String fileName = fileNameNoSuffix + File.separator + fileId;
            filePath = appConfig.getProjectFolder() + SystemConstants.FILE_FOLDER_FILE + fileName;
        } else {
            FileInfo fileInfo = fileInfoService
                    .lambdaQuery()
                    .eq(FileInfo::getFileId, fileId)
                    .eq(FileInfo::getUserId, userId)
                    .one();
            System.out.println("fileInfo = " + fileInfo);
            if (null == fileInfo) {
                return Result.fail("文件不存在", CodeEnum.NOT_FOUND);
            }
            if (FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                // 视频文件 读取 m3u8分片文件索引文件
                // m3u8 文件夹名  为  视频文件名 去掉后缀
                String fileNameNoSuffix = fileInfo.getFilePath().substring(0, fileInfo.getFilePath().lastIndexOf("."));
                String fileName = fileNameNoSuffix + File.separator + SystemConstants.M3U8_NAME;
                // m3u8 路径
                filePath = appConfig.getProjectFolder() + SystemConstants.FILE_FOLDER_FILE + fileName;
            } else {
                // 其他文件
                filePath = appConfig.getProjectFolder() + SystemConstants.FILE_FOLDER_FILE + fileInfo.getFilePath();
            }
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return Result.fail("文件不存在", CodeEnum.NOT_FOUND);
        }
        // 响应文件
        com.zhang.mypan.utils.FileUtil.responseFile(response, file);
        return Result.ok();
    }

    @Override
    public Result newFolder(String filePid, String userId, String fileName) {
        // 检测是否重名
        checkFileName(filePid, userId, fileName, FileFolderTypeEnums.FOLDER.getType());
        //
        Date curDate = new Date();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUserId(userId);
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(fileName);
        fileInfo.setFileId(RandomUtil.randomString(SystemConstants.RANDOM_FILE_ID_LEN));
        fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        save(fileInfo);
        return Result.ok(FileDto.parseFileDto(fileInfo));
    }

    @Override
    public Result getFolderInfo(String path, String userId) {
        String[] pathArray = path.split("/");
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFolderType, FileFolderTypeEnums.FOLDER.getType());
        if (userId != null) {
            wrapper.eq(FileInfo::getUserId, userId);
        }
        wrapper.in(FileInfo::getFileId, Arrays.asList(pathArray))
                .last("order by field(file_id,\"" + String.join("\",\"", pathArray) + "\")");
        final List<FileDto> list = list(wrapper).stream().map(FileDto::parseFileDto).collect(Collectors.toList());
        return Result.ok(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result rename(String fileId, String userId, String fileName) {
        FileInfo fileInfo = selectByFileIdAndUserId(fileId, userId);
        if (null == fileInfo) {
            throw new MyPanException("文件不存在", CodeEnum.NOT_FOUND);
        } else {
            String filePid = fileInfo.getFilePid();
            checkFileName(filePid, userId, fileName, fileInfo.getFolderType());
            // 获取文件后缀
            if (FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFolderType())) {
                String fileAllName = fileInfo.getFileName();
                fileName = fileAllName.substring(0, fileAllName.lastIndexOf("."));
            }
            Date curDate = new Date();
            fileInfo.setFileName(fileName);
            fileInfo.setLastUpdateTime(curDate);
            log.info("修改的文件信息：\n" +
                    "文件ID：" + fileId +
                    "\n用户ID：" + fileInfo.getUserId() +
                    "\n文件名：" + fileInfo.getFileName());
            LambdaUpdateWrapper<FileInfo> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(FileInfo::getFileId, fileId)
                    .set(FileInfo::getFileName, fileInfo.getFileName())
                    .set(FileInfo::getLastUpdateTime, fileInfo.getLastUpdateTime());
            update(wrapper);
            // 二次查看重名，防止并发
            FileInfoQuery fileInfoQuery = new FileInfoQuery();
            fileInfoQuery.setFolderType(fileInfo.getFolderType());
            fileInfoQuery.setFileName(fileInfo.getFileName());
            fileInfoQuery.setUserId(userId);
            final LambdaQueryWrapper<FileInfo> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(FileInfo::getFilePid, filePid)
                    .eq(FileInfo::getUserId, userId)
                    .eq(FileInfo::getFileName, fileName);
            long count = count(wrapper1);
            if (count > 1) {
                throw new MyPanException("文件重名，请重新设置");
            }
            return Result.ok(FileDto.parseFileDto(fileInfo));
        }
    }

    @Override
    public Result loadAllFolder(String filePid, String userId, String currentFileIds) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFilePid, filePid)
                .eq(FileInfo::getFolderType, FileFolderTypeEnums.FOLDER.getType())
                .eq(FileInfo::getDelFlag, FileDelFlagEnums.USING.getFlag());
        // 过滤掉自己所在的目录
        if (!StringUtils.isEmpty(currentFileIds)) {
            wrapper.notIn(FileInfo::getFileId, currentFileIds);
        }
        wrapper.orderByDesc(FileInfo::getLastUpdateTime);
        List<FileDto> fileDtos = list(wrapper).stream().map(FileDto::parseFileDto).collect(Collectors.toList());
        return Result.ok(fileDtos);
    }

    @Override
    public Result changeFileFolder(String fileIds, String userId, String filePid) {
        if (fileIds.equals(filePid)) {
            throw new MyPanException("不能移动到自身或子目录");
        }
        if (!SystemConstants.ROOT_FOLDER_ID.equals(filePid)) {
            // 不是根目录
            final FileInfo pFileInfo = selectByFileIdAndUserId(filePid, userId);
            if (pFileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(pFileInfo.getDelFlag())) {
                throw new MyPanException(CodeEnum.BAD_REQUEST);
            }
        }
        String[] fileIdArray = fileIds.split(",");
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFilePid, filePid)
                .eq(FileInfo::getUserId, userId);
        final Map<String, FileInfo> dbFileNameMap = list(wrapper).stream()
                .collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (d1, d2) -> d2));
        // 查询选中的文件
        LambdaQueryWrapper<FileInfo> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(FileInfo::getUserId, userId)
                .in(FileInfo::getFileId, Arrays.asList(fileIdArray));
        final List<FileInfo> list = list(wrapper1);
        for (FileInfo fileInfo : list) {
            final FileInfo selectFile = dbFileNameMap.get(fileInfo.getFileName());
            String newFileName = fileInfo.getFileName();
            LambdaUpdateWrapper<FileInfo> wrapperSelect = new LambdaUpdateWrapper<>();
            if (selectFile != null) {
                // 文件名已经存在
                newFileName = selectFile.getFileName() + "-" + UUID.randomUUID();
            }
            Date curDate = new Date();
            wrapperSelect.eq(FileInfo::getFileId, fileInfo.getFileId())
                    .eq(FileInfo::getUserId, userId)
                    .set(FileInfo::getFileName, newFileName)
                    .set(FileInfo::getFilePid, filePid)
                    .set(FileInfo::getLastUpdateTime, curDate);
            update(wrapperSelect);
        }
        return Result.ok();
    }

    @Override
    public Result createDownloadUrl(String fileId, String userId) {
        final FileInfo fileInfo = selectByFileIdAndUserId(fileId, userId);
        if (null == fileInfo) {
            // 错误的文件信息
            throw new MyPanException("文件信息错误", CodeEnum.BAD_REQUEST);
        }
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            // 文件夹不能下载
            throw new MyPanException("文件夹不能下载", CodeEnum.BAD_REQUEST);
        }
        // 设置随机数
        String code = RandomUtil.randomString(RedisConstants.DOWNLOAD_RANDOM_STR_LEN);
        //
        DownloadDTO downloadDTO = new DownloadDTO(fileInfo);
        final Object codeMap = redisUtil.get(RedisConstants.DOWNLOAD_FILEID_MAP + fileId);
        if (null != codeMap) {
            // 已经存在
            code = (String) codeMap;
        } else {
            // 保存到redis (code-->filePath,fileId-->code)
            saveDownloadCode2redius(fileId, code, downloadDTO);
        }
        return Result.ok(code);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        DownloadDTO downloadDTO = redisUtil.get(RedisConstants.DOWNLOAD_CODE_KEY + code, DownloadDTO.class);
        if (null == downloadDTO) {
            log.info("下载码{}不存在或已失效", code);
            throw new MyPanException("下载码不存在或已失效", CodeEnum.BAD_REQUEST);
        }
        String filePath = downloadDTO.getFilePath();
        String fileName = downloadDTO.getFileName();
        response.setContentType("application/x-msdownload;charset=UTF-8");
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
            // ie
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        com.zhang.mypan.utils.FileUtil.responseFile(response, appConfig.getProjectFolder() +
                SystemConstants.FILE_FOLDER_FILE + filePath);
    }

    @Override
    public Result delFile(String userId, String fileIds) {
        String[] fileIdArray = fileIds.split(",");
        // 查找这个文件
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getDelFlag, FileDelFlagEnums.USING.getFlag())
                .in(FileInfo::getFileId, Arrays.asList(fileIdArray));
        final List<FileInfo> list = list(wrapper);
        if (list.isEmpty()) {
            return null;
        }

        // 将选中的文件更新为回收栈(将所有的选中的文件和文件夹设置为回收栈中)
        // 这个放入回收栈只是将文件夹放入回收栈
        // 其子文件没有改变（但是在main界面不会显示，因为做了过滤）
        List<String> delFileIdList = Arrays.asList(fileIdArray);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        this.baseMapper.updateFileDelFlagBatch(fileInfo, userId, null, delFileIdList, FileDelFlagEnums.USING.getFlag());

        return Result.ok("已放入回收站");
    }

    @Override
    public Result loadRecycleList(String userId, Integer pageNo, Integer pageSize) {
        FileInfoQuery query = new FileInfoQuery();
        query.setPageSize(pageSize);
        query.setPageNo(pageNo);
        query.setUserId(userId);
        query.setOrderBy("recovery_time desc");
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        final PaginationResultVO<FileDto> pagelist = pagelist(query);
        return Result.ok(pagelist);
    }

    @Override
    public Result recoverFile(String userId, String fileIds) {
        String[] fileIdArray = fileIds.split(",");
        // 先查找有没有同名文件
        List<FileInfo> sameNameFileList = this.baseMapper.selectSameNameFile(userId, fileIdArray, FileDelFlagEnums.DEL.getFlag());
        if (!sameNameFileList.isEmpty()) {
            return Result.fail("恢复失败，在路径{" +
                    sameNameFileList.get(0).getFilePath()
                    + "}存在同名文件{" +
                    sameNameFileList.get(0).getFileName() + "}", CodeEnum.BAD_REQUEST);
        }
        LambdaUpdateWrapper<FileInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(FileInfo::getDelFlag, FileDelFlagEnums.USING.getFlag())
                .set(FileInfo::getLastUpdateTime, new Date())
                .eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getDelFlag, FileDelFlagEnums.RECYCLE.getFlag())
                .in(FileInfo::getFileId, Arrays.asList(fileIdArray));
        // 回收就将 选中的文件 再设置为USING
        // 因为子文件没有改变 不寻妖递归，只有真正删除的时候会递归
        final boolean success = update(wrapper);
        if (!success) return Result.fail("恢复失败,可能已经超时", CodeEnum.NOT_FOUND);
        return Result.ok("恢复成功");
    }

    @Override
    public Result delFileReal(String userId, String fileIds) {
        String[] fileIdArray = fileIds.split(",");
        // 查找这个文件
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
                .ne(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag())
                .in(FileInfo::getFileId, Arrays.asList(fileIdArray));
        final List<FileInfo> list = list(wrapper);
        if (list.isEmpty()) {
            return Result.fail("删除失败", CodeEnum.NOT_FOUND);
        }
        delFileReal1(userId, list);

        return Result.ok("删除成功");
    }

    @Override
    @Transactional
    public void delFileReal1(String userId, List<FileInfo> list) {
        // 查找所有要删除的文件以及文件夹（递归）
        List<String> delFileList = new ArrayList<>();
        // 存储删除文件总大小
        long[] totalSize = {0};
        forEachRecycleFile(userId, list, delFileList, totalSize);
        System.out.println("要删除的文件：" + delFileList);
        if (!delFileList.isEmpty()) {
            // 真正删除 （逻辑删除）
            FileInfo updateFileInfo = new FileInfo();
            updateFileInfo.setRecoveryTime(new Date());
            updateFileInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
            this.baseMapper.updateFileDelFlagBatch(updateFileInfo, userId, null, delFileList, null);
            // 更新用户使用空间大小
            log.info("删除的文件大小" + totalSize[0]);
            userService.setUserSpace(userId, -totalSize[0], null);

            // 过滤掉所有不重复的MD5的文件（有秒传的文件）
            List<String> realDelFileList = fileInfoService.filterSecondUploadFile(delFileList);
            System.out.println("去重后要删除的文件" + realDelFileList);
            // 将所有要删除的文件数据的md5 设置为 -1
            boolean success = lambdaUpdate()
                    .set(FileInfo::getFileMd5, SystemConstants.REAL_DEL_FILE_MD5)
                    .in(FileInfo::getFileId, delFileList)
                    .update();
            // 删除实际文件(异步) mq 处理
            if (success && !realDelFileList.isEmpty()) {
                DelFileDTO delFileDTO = new DelFileDTO(userId, realDelFileList);
                rabbitTemplate.convertAndSend(mqDelFileQueue, delFileDTO);
            }
        }
    }

    @Override
    public void deleteAllFiles(String userId) {
        // 查找所有要删除的文件以及文件夹
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
                .ne(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag());
        final List<FileInfo> list = list(wrapper);
        // 修改所有文件为删除
        LambdaUpdateWrapper<FileInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag())
                .eq(FileInfo::getUserId, userId)
                .ne(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag());
        // 检测redis
        final UserDTO userDTO = redisUtil.get(RedisConstants.USER_LOGIN_KEY + userId, UserDTO.class);
        if (userDTO != null) {
            userDTO.setUserUseSpace(0L);
            redisUtil.set(RedisConstants.USER_LOGIN_KEY + userId, userDTO);
        }
        // 异步删除文件
        List<String> fileIds = list.stream().map(FileInfo::getFileId).collect(Collectors.toList());
        rabbitTemplate.convertAndSend(SystemConstants.MQ_DELFILE_QUEUE, new DelFileDTO(userId, fileIds));
    }

    @Override
    public Result loadFileList(FileInfoQuery query) {
        List<FileDto> list = this.baseMapper.loadFileList(query.getFilePid(), query.getFileNameFuzzy());
        final Page<FileDto> page = PageUtil.page(list, query.getPageNo(), query.getPageSize());
        System.out.println(list);
        return Result.ok(new PaginationResultVO<>(page.getTotal(), page.getSize(), page.getCurrent(), page.getPages(), page.getRecords()));
    }

    @Override
    public Result loadFileList1(FileInfoQuery query) {
        List<FileDto> list = this.baseMapper.loadFileList1(query.getFilePid(), query.getFileId(), query.getUserId());
        System.out.println(list);
        final Page<FileDto> page = PageUtil.page(list, query.getPageNo(), query.getPageSize());
        return Result.ok(new PaginationResultVO<>(page.getTotal(), page.getSize(), page.getCurrent(), page.getPages(), page.getRecords()));
    }

    @Override
    public List<String> filterSecondUploadFile(List<String> delFileList) {
        return baseMapper.filterSecondUploadFile(delFileList);
    }


    /**
     * Del file.
     *
     * @param delFileDTO the del file dto
     */
    @Async
    @RabbitListener(queues = mqDelFileQueue)
    public void delFile(DelFileDTO delFileDTO) {
        // 处理要删除的文件
        System.out.println("正在异步处理删除文件：" + delFileDTO);
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, delFileDTO.getUserId())
                .in(FileInfo::getFileId, delFileDTO.getDelFileList());
        final List<FileInfo> files = list(wrapper);
        String basePath = appConfig.getProjectFolder() + SystemConstants.FILE_FOLDER_FILE;
        for (FileInfo file : files) {
            String subPath = file.getFilePath();
            if (subPath != null) {
                String path = basePath + subPath;
                final File file1 = new File(path);
                // 普通文件
                if (file1.exists()) {
                    file1.delete();
                }
                // 视频文件

                if (file.getFileType().equals(FileTypeEnums.VIDEO.getType())) {
                    // 封面图
                    final String coverPath = subPath.replace(subPath.substring(subPath.lastIndexOf(".")), ".png");
                    final File coverFile = new File(basePath + coverPath);
                    if (coverFile.exists()) {
                        coverFile.delete();
                    }
                    // 分片文件夹
                    String videoFolderPath = subPath.substring(0, subPath.lastIndexOf("."));
                    final File folder = new File(basePath + videoFolderPath);
                    System.out.println("分片文件夹路径：" + basePath + videoFolderPath);
                    if (folder.exists()) {
                        com.zhang.mypan.utils.FileUtil.delFolder(folder);
                    }
                }
                // 图片文件
                if (file.getFileType().equals(FileTypeEnums.IMAGE.getType())) {
                    // 封面
                    final String coverPath = subPath.replace(subPath.substring(subPath.lastIndexOf(".")), "_.png");
                    final File coverFile = new File(basePath + coverPath);
                    if (coverFile.exists()) {
                        coverFile.delete();
                    }
                }

            }
        }
    }

    private void forEachRecycleFile(String userId, List<FileInfo> list, List<String> delFileList, long[] totalSize) {
        for (FileInfo fileInfo : list) {
            Long fileSize = fileInfo.getFileSize();
            if (fileSize != null) totalSize[0] += fileSize;
            if (fileInfo.getFolderType().equals(FileFolderTypeEnums.FOLDER.getType())) {
                findAllSubFolderAndFile(delFileList, totalSize, userId, fileInfo.getFileId());
            } else {
                delFileList.add(fileInfo.getFileId());
            }
        }
    }

    /**
     * 递归查找所有子文件 保存到delFileList 中
     *
     * @param delFileList 添加到list
     * @param userId
     * @param fileId
     */
    @Override
    public void findAllSubFolderAndFile(List<String> delFileList, long[] totalSize, String userId, String fileId) {
        delFileList.add(fileId);
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFilePid, fileId);
        final List<FileInfo> subFolderList = list(wrapper);
        forEachRecycleFile(userId, subFolderList, delFileList, totalSize);
    }

    // 保存到redius
    private void saveDownloadCode2redius(String fileId, String code, DownloadDTO downloadDTO) {
        redisUtil.set(RedisConstants.DOWNLOAD_CODE_KEY + code,
                downloadDTO,
                RedisConstants.DOWNLOAD_CODE_EXPIRE_TIME);
        redisUtil.set(RedisConstants.DOWNLOAD_FILEID_MAP + fileId, code,
                RedisConstants.DOWNLOAD_CODE_EXPIRE_TIME);
    }

    private FileInfo selectByFileIdAndUserId(String fileId, String userId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileId, fileId)
                .eq(FileInfo::getUserId, userId);
        return getOne(wrapper);
    }

    /**
     * 检测新建的文件夹有没有同名的文件名
     */
    private void checkFileName(String filePId, String userId, String fileName, Integer folderType) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFolderType(folderType);
        fileInfoQuery.setFileName(fileName);
        fileInfoQuery.setUserId(userId);
        final LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFilePid, filePId)
                .eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFileName, fileName)
                .ne(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag());
        long count = count(wrapper);
        if (count > 0) {
            throw new MyPanException("此目录下文件重名，请重新设置");
        }

    }

    /**
     * 获取分页数据
     */
    private PaginationResultVO<FileDto> pagelist(FileInfoQuery query) {
        final String fileNameFuzzy = query.getFileNameFuzzy();
        System.out.println(fileNameFuzzy);
        QueryChainWrapper<FileInfo> tmp = query()
                // 用户id
                .eq("user_id", query.getUserId())
                // 删除标志
                .eq("del_flag", query.getDelFlag());
        if (query.getFilePid() != null) {
            // 文件夹Id
            tmp.eq("file_pid", query.getFilePid());
        }
        if (query.getFileCategory() != null) {
            // 设置查询的类别
            tmp.eq("file_category", query.getFileCategory());
        }
        if (fileNameFuzzy != null) {
            tmp.like("file_name", fileNameFuzzy);
        }
        // 查询顺序
        final Page<FileInfo> filePage = tmp.last("order by " + query.getOrderBy())
                // 分页
                .page(new Page<>(query.getPageNo(), query.getPageSize()));
        // 将FileInfo数据映射未FileDto数据
        List<FileDto> fileData = filePage
                .getRecords()
                .stream()
                .map(FileDto::parseFileDto)
                .collect(Collectors.toList());
        // 返回
        return new PaginationResultVO<FileDto>(filePage.getTotal(),
                filePage.getSize(), filePage.getCurrent(), filePage.getPages(), fileData);
    }


    /**
     * 异步转码文件
     *
     * @param fileId  the file id
     * @param userDTO the user dto
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void asyncTransferFile(String fileId, UserDTO userDTO) {
        boolean transferSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileTypeEnums fileTypeEnums = null;
        FileInfo fileInfo = this.query()
                .eq("file_id", fileId)
                .eq("user_id", userDTO.getId())
                .one();
        try {
            // 不是转码中
            if (fileInfo == null || !FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
                return;
            }
            // 临时目录
            String tempFolderName = appConfig.getProjectFolder() + SystemConstants.FILE_TMP_FOLDER;
            String currentUserFolderName = userDTO.getId() + fileId;
            // tmp/userId fileid/
            File fileFolder = new File(tempFolderName + currentUserFolderName);

            String fileName = fileInfo.getFileName();
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String month = new SimpleDateFormat("yyyyMM").format(fileInfo.getCreateTime());
            // 目标目录
            String targetFolderName = appConfig.getProjectFolder() + SystemConstants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + File.separator + month);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            // 真实的文件名
            String realFileName = currentUserFolderName + suffix;
            targetFilePath = targetFolder.getPath() + File.separator + realFileName;

            // 合并文件
            union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
            // 视频文件的切割
            fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(suffix);
            if (FileTypeEnums.VIDEO == fileTypeEnums) {
                // 视频 切割生成缩略图
                cutFile4Video(fileId, targetFilePath);
                // 生成缩略图
                cover = month + File.separator + currentUserFolderName + SystemConstants.IMAGE_PNG_SUFFIX;
                String coverPath = targetFolderName + File.separator + cover;
                ScaleFilter.createCover4Video(new File(targetFilePath), SystemConstants.IMAGE_WIDTH, new File(coverPath));

            } else if (FileTypeEnums.IMAGE == fileTypeEnums) {
                // 图片
                // 生成缩略图  源文件.xxx  ===>  源文件_.xxx
                cover = month + File.separator + realFileName.replace(".", "_.");
                String coverPath = targetFolderName + File.separator + cover;

                Boolean created = ScaleFilter.createThumbnailWithFFmpeg(new File(targetFilePath),
                        SystemConstants.IMAGE_WIDTH,
                        new File(coverPath),
                        false);
                if (!created) {
                    // 生成缩略图失败，复制原图为缩略图
                    FileUtil.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }

        } catch (Exception e) {
            log.error("文件转码失败,文件ID{}，userId{}", fileId, userDTO.getId(), e);
            transferSuccess = false;
        } finally {
            FileInfo updateInfo = new FileInfo();
            if (targetFilePath != null) {
                updateInfo.setFileSize(new File(targetFilePath).length());
                updateInfo.setFileCover(cover); //设置封面
                updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
                LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(FileInfo::getFileId, fileId)
                        .eq(FileInfo::getUserId, userDTO.getId())
                        .eq(FileInfo::getStatus, FileStatusEnums.TRANSFER.getStatus());
                update(updateInfo, wrapper);
            }
        }
    }

    /**
     * 视频切割
     *
     * @param fileId
     * @param videoFilePath
     */
    private void cutFile4Video(String fileId, String videoFilePath) {
        // 创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        // 不存在创建
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        // ffmpeg命令(先转为TS，再切割)
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time "+SystemConstants.VIDEO_CHUNK_SIZE+" %s/%s_%%4d.ts";

        String tsPath = tsFolder + File.separator + SystemConstants.TS_NAME;
        // 生成.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);
        // 生产索引文件.m3u8 和 切片 .ts 文件
        cmd = String.format(CMD_CUT_TS, tsPath,
                tsFolder.getPath() + File.separator + SystemConstants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        // 删除index.ts 文件
        new File(tsPath).delete();

    }


    /**
     * 合并文件
     *
     * @param dirPath    分片文件所在的目录
     * @param toFilePath 合并文件的路径
     * @param fileName   文件名
     * @param delSource  是否删除之前的文件目录
     */
    private void union(String dirPath, String toFilePath, String fileName, Boolean delSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new MyPanException("目录不存在");
        }
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        try (RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw")) {
            byte[] buffer = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                File chunkFile = new File(dirPath + File.separator + i);
                try (RandomAccessFile readFile = new RandomAccessFile(chunkFile, "r")) {
                    while ((len = readFile.read(buffer)) != -1) {
                        writeFile.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new MyPanException("合并分片失败");
                }
            }

        } catch (Exception e) {
            log.error("合并{}文件失败", fileName, e);
            throw new MyPanException("合并文件失败");
        } finally {
            if (delSource && dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    log.error("删除临时目录失败", e);
                    throw new MyPanException("删除临时目录失败");
                }
            }
        }
    }


    /**
     * Sets file info service.
     *
     * @param fileInfoService the file info service
     */
    @Autowired
    @Lazy
    public void setFileInfoService(FileInfoServiceImpl fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    /**
     * Sets user service.
     *
     * @param userService the user service
     */
    @Autowired
    @Lazy // 延迟加载 解决循环依赖
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Sets app config.
     *
     * @param appConfig the app config
     */
    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Sets redis util.
     *
     * @param redisUtil the redis util
     */
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * Sets rabbit template.
     *
     * @param rabbitTemplate the rabbit template
     */
    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}




