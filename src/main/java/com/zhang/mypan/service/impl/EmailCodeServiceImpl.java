package com.zhang.mypan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zhang.mypan.config.AppConfig;
import com.zhang.mypan.dto.SysSettingDto;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.dto.Result;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.service.EmailCodeService;
import com.zhang.mypan.utils.RedisConstants;
import com.zhang.mypan.utils.RedisUtil;
import com.zhang.mypan.utils.SessionConstants;
import com.zhang.mypan.utils.SystemConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


/**
 * @author 86180
 * @description 针对表【email_code】的数据库操作Service实现
 * @createDate 2024-03-17 10:05:48
 */
@Service
@Slf4j
public class EmailCodeServiceImpl implements EmailCodeService {
    private JavaMailSender javaMailSender;
    private AppConfig appConfig;
    private RedisUtil redisUtil;
    private RabbitTemplate rabbitTemplate;
    public static final String msgQueue = SystemConstants.MQ_MSG_QUEUE;

    @Async
    @RabbitListener(queues = msgQueue)
    public void handleMessage(MSG msg) {
        // 处理
        log.info("正在快速处理消息" + msg);
        sendEmail(msg.toEmail, msg.emailCode);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result sendEmailCode(HttpSession session, String email, String checkCode, Integer type) {
        try {
            // 检查图片验证码
            String sessionCode = (String) session.getAttribute(SessionConstants.IMAGE_CODE);
            if (StrUtil.isBlank(sessionCode) || !sessionCode.equals(checkCode)) {
                // 图片验证码错误
                return Result.fail("验证码错误", CodeEnum.BAD_REQUEST);
            }
            // 成功发送邮箱验证码
            if (type == 0) {
                final String key = RedisConstants.EMAIL_CODE_KEY + email;
                // 注册 在redis中查找
                String code = (String) redisUtil.get(key);
                if (null != code) {
                    // 邮箱已存在，提示用户先登录
                    return Result.fail("已发送至您的邮箱", CodeEnum.CONFILCT);
                }
                // 生成随机验证码
                final String emailcode = getRandomCode();
                // 发送验证码  通过mq异步发送
                final MSG msg = new MSG(email, emailcode);
                rabbitTemplate.convertAndSend(msgQueue, msg);

                log.info("邮件验证码为:{}", emailcode);
                // 存入redis
                redisUtil.set(key, emailcode, RedisConstants.EMAIL_CODE_TTL);
            }
        } catch (Exception e) {
            log.error("发送失败", e);
            throw new MyPanException("发送失败");
        } finally {
            // 删除之前的图片验证码
            session.removeAttribute(SessionConstants.IMAGE_CODE);
        }
        return Result.ok("发送成功，请登录邮箱查看");

    }

    /**
     * 利用QQ邮箱发送验证码
     *
     * @param toEmail
     * @param emailcode
     */
    private void sendEmail(String toEmail, String emailcode) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom(appConfig.getSendUserName());
            mimeMessageHelper.setTo(toEmail);
            // 从redis中获取email设置
            // EmailSettingDto emailSetting = redisUtil.get(RedisConstants.EMAIL_SETTING_KEY, EmailSettingDto.class);
            SysSettingDto sysSettingDto = redisUtil.get(RedisConstants.SYS_SETTING_KEY, SysSettingDto.class);

            // 标题
            if (Objects.isNull(sysSettingDto)) {
                log.error("程序员需要添加邮件配置信息到redis中");
                throw new NullPointerException();
            }
            mimeMessageHelper.setSubject(sysSettingDto.getRegisterEmailTitle());
            // 内容
            mimeMessageHelper.setText(String.format(sysSettingDto.getRegisterEmailContent(), emailcode), true);
            // 发送时间
            mimeMessageHelper.setSentDate(new Date());
            // 发送  通过mq异步发送
            javaMailSender.send(message);
            // 获取邮件服务器响应码

        } catch (Exception e) {
            log.error("邮件发送失败", e);
        }
    }


    /**
     * 获取长度为len的验证码
     *
     * @return
     */
    private String getRandomCode() {
        return RandomStringUtils.randomNumeric(SystemConstants.EMAIL_CODE_LEN);
    }

    @Autowired
    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MSG implements Serializable {
        private static final long serialVersionUID = 1L;
        private String toEmail;
        private String emailCode;

    }
}




