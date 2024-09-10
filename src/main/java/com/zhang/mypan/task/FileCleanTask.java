package com.zhang.mypan.task;

import com.zhang.mypan.entity.FileInfo;
import com.zhang.mypan.enums.FileDelFlagEnums;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileCleanTask {
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    private static final String mqDelFileQueue = SystemConstants.MQ_DELFILE_QUEUE;

    /**
     * 定时任务 (清理回收站中超时的文件)
     */
    @Scheduled(fixedDelay = 1000 * 60 * 3) // 3分钟
    public void execute() {
        System.out.println("定时任务执行");
        // 先查找
        final boolean exists = fileInfoService.lambdaQuery()
                .eq(FileInfo::getDelFlag, FileDelFlagEnums.RECYCLE.getFlag())
                .le(FileInfo::getRecoveryTime, LocalDateTime.now().minusDays(10)).exists();
        if (!exists) {
            log.info("回收站没有要处理的文件");
            return;
        }
        // 再删除文件
        final boolean success = fileInfoService.lambdaUpdate()
                .set(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag())
                .eq(FileInfo::getDelFlag, FileDelFlagEnums.RECYCLE.getFlag())
                .le(FileInfo::getRecoveryTime, LocalDateTime.now().minusDays(10))
                .update();
        if (success) {
            // 再查找文件数据(逻辑删除的数据，为下面异步删除做准备)
            final List<FileInfo> list = fileInfoService.lambdaQuery()
                    .eq(FileInfo::getDelFlag, FileDelFlagEnums.DEL.getFlag())
                    .ne(FileInfo::getFileMd5, SystemConstants.REAL_DEL_FILE_MD5)
                    .le(FileInfo::getRecoveryTime, LocalDateTime.now().minusDays(10))
                    .list();
            if (list.isEmpty()) {
                log.error("回收站超时处理失败");
                return;
            }
            final HashMap<String, List<FileInfo>> map = list.stream()
                    .collect(Collectors.groupingBy(FileInfo::getUserId, HashMap::new, Collectors.toList()));
            System.out.println("要删除的文件" + map);
            // 异步删除mq
            if (map.isEmpty()) return;
            for (Map.Entry<String, List<FileInfo>> userIdAndFileInfos : map.entrySet()) {
                fileInfoService.delFileReal1(userIdAndFileInfos.getKey()
                        , userIdAndFileInfos.getValue());
            }
        }

    }
}
