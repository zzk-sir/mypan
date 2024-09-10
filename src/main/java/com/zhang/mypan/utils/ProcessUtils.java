package com.zhang.mypan.utils;

import com.zhang.mypan.exception.MyPanException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

@Slf4j
public class ProcessUtils {

    /**
     * 执行ffmpeg命令，
     *
     * @param cmd         命令
     * @param outprintLog 是否输出日志
     * @return
     */
    public static String executeCommand(String cmd, Boolean outprintLog) throws MyPanException {
        if (StringUtils.isEmpty(cmd)) {
            log.error("---指令执行失败，因为要执行的FFmpeg指令为空！---");
            return null;
        }

        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            // 执行ffmpeg
            // 取出输出流和错误信息
            // 注意，必须要取出ffmpeg再执行命令过程中生产的输出信息，如果不取出的话当输出流信息填满JVM存储输出流信息的缓存区时，
            // 线程就会阻塞
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            PrintStream inputStream = new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            // 等待ffpeg命令执行完
            process.waitFor();
            // 获取执行的命令信息
            String result = errorStream.stringBuffer.append(inputStream.stringBuffer + "\n").toString();
            // 输出命令信息
            if (outprintLog) {
                log.info("---指令{}执行结果：{}---", cmd, result);
            } else {
                log.info("---指令{}执行完毕", cmd);
            }
            return result;
        } catch (Exception e) {
            log.error("---指令{},执行失败", e);
            throw new MyPanException("视频转换失败");
        } finally {
            if (null != process) {
                ProcessKiller ffmpegKiller = new ProcessKiller(process);
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }

    /**
     * 再程序退出前结束已有的FFmpeg进程
     */
    private static class ProcessKiller extends Thread {
        private Process process;

        public ProcessKiller(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            this.process.destroy();
        }
    }

    /**
     * 用于取出ffmpeg线程执行过程中产生的各种输出和各种错误流的信息
     */
    static class PrintStream extends Thread {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();

        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                if (null == inputStream) {
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (Exception e) {
                log.error("读取输入流错误了！错误信息", e);

            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("调用PrintStream读取输出流后，关闭流时出现错误！");
                }
            }

        }
    }

}
