package com.zhang.mypan.utils;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.zhang.mypan.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FileUtil {

    public static final Map<String, String> imgtype = new HashMap<>();

    static {
        imgtype.put("image/jpeg", ".jpeg");
        imgtype.put("image/jpg", ".jpg");
        imgtype.put("image/png", ".png");
        imgtype.put("image/gif", ".gif");
        imgtype.put("image/svg+xml", ".svg");
    }

    /**
     * 通过文件路径名获取文件类型
     *
     * @param filePath
     * @return
     */
    public static String getTypeByFilePath(String filePath) {
        if (!filePath.contains(".") || !pathIsOk(filePath)) {
            log.error("文件路径错误");
            return null;
        }
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    /**
     * 通过文件夹路径随机获取图片
     *
     * @param folderPath 这个文件夹下必须都是图片类
     * @return
     */
    public static File getRandomImgOfFolder(String folderPath) {
        if (!pathIsOk(folderPath)) {
            log.error("文件夹路径错误");
            return null;
        }
        File folder = new File(folderPath);
        if (!folder.exists()) {
            log.error("文件夹不存在");
            return null;
        }
        if (!folder.isDirectory()) {
            log.error("参数不是正确的文件夹路径");
            return null;
        }
        File[] files = folder.listFiles();
        if (ArrayUtil.isEmpty(files)) {
            log.error("文件夹为空");
            return null;
        }
        int randomIndex = (int) (Math.random() * files.length);
        return files[randomIndex];
    }

    public static boolean pathIsOk(String filePath) {
        if (StrUtil.isEmpty(filePath)) {
            return true;
        }
        return !filePath.contains("../") && !filePath.contains("..\\");
    }

    /**
     * 将文件流写入到response中
     *
     * @param response
     * @param filePath
     */
    public static void responseFile(HttpServletResponse response, String filePath) {
        if (!pathIsOk(filePath)) {
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                log.error("IO异常", e);
            }
        }
    }

    public static void responseFile(HttpServletResponse response, File file) {
        OutputStream out = null;
        FileInputStream in = null;
        try {
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                log.error("IO异常", e);
            }
        }
    }

    public static String getSuffixFromContentType(String contentType) {
        if (imgtype.containsKey(contentType)) {
            return imgtype.get(contentType);
        }
        return null;
    }


    /**
     * 从 可访问的文件路径中获取 Content-Type
     *
     * @param fileUrlPath
     * @return
     * @throws IOException
     */
    public static String getContentType(String fileUrlPath) throws IOException {
        URL url = new URL(fileUrlPath);
        URLConnection connection = url.openConnection();
        return connection.getContentType();
    }

    /**
     * 通过读取文件并获取其width及height的方式，来判断判断当前文件是否图片，这是一种非常简单的方式。
     *
     * @param imageFile
     * @return
     */
    public static boolean isImage(File imageFile) {
        if (!imageFile.exists()) {
            return false;
        }
        Image img = null;
        try {
            img = ImageIO.read(imageFile);
            if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            img = null;
        }
    }

    /**
     * 获取图片到 响应流
     *
     * @param response
     * @param imageFolder
     * @param imageName
     * @param appConfig
     */
    public static void getImage(HttpServletResponse response, String imageFolder, String imageName, AppConfig appConfig) {
        if (StringUtils.isEmpty(imageFolder) ||
                StringUtils.isEmpty(imageName) ||
                !pathIsOk(imageFolder) ||
                !pathIsOk(imageName)) {
            return;
        }
        String imageSuffix = imageName.substring(imageName.lastIndexOf(".") + 1);
        String filePath = appConfig.getProjectFolder() + SystemConstants.FILE_FOLDER_FILE + imageFolder + File.separator + imageName;
        System.out.println("图片路径：" + filePath);
        response.setContentType("image/" + imageSuffix);
        response.setHeader("Cache-Control", "max-age=2592000");
        responseFile(response, filePath);
    }

    /**
     * 删除非空文件夹
     *
     * @param folder
     */
    public static void delFolder(File folder) {
        if (folder.isDirectory()) {
            final File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) delFolder(file);
                else file.delete();
            }
            // 删除空文件夹
            folder.delete();
        }
    }
}
