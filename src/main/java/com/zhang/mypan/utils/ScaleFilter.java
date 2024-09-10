package com.zhang.mypan.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class ScaleFilter {
    // 生成封面
    public static void createCover4Video(File sourceFile, Integer width, File targetFile) {
        try {
            String cmd = "ffmpeg -i %s -vframes 1 -vf scale=%d:%d/a %s";
            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsolutePath(), width, width, targetFile.getAbsolutePath()), false);

        } catch (Exception e) {
            log.error("生成视频封面失败", e);
        }
    }

    public static Boolean createThumbnailWithFFmpeg(File file, int thumbnailWidth, File targetFile, Boolean delSource) {
        try {
            BufferedImage src = ImageIO.read(file);
            // thumbnailWidth 缩略图的宽度 thumbnailHeight 缩略图的高度
            int sorceW = src.getWidth();
            int sorceH = src.getHeight();
            // 小于 指定高度不压缩
            if (sorceW <= thumbnailWidth) {
                return false;
            }
            compressImage(file, thumbnailWidth, targetFile, delSource);
            return true;
        } catch (Exception e) {
            log.error("生成图片缩略图失败", e);
        }
        return false;
    }

    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
        try {
            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsolutePath(), width, targetFile.getAbsolutePath()), false);
            if (delSource) {
                FileUtils.forceDelete(sourceFile);
            }
        } catch (Exception e) {

        }
    }


}
