package com.zhang.mypan.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhang.mypan.utils.RedisConstants;
import com.zhang.mypan.utils.SystemConstants;
import lombok.Data;
import lombok.ToString;

/**
 * 可以设置用户注册数据
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) //用于在 JSON 反序列化过程中忽略未知的属性
public class SysSettingDto {
    private static final long serialVersionUID = 1L;
    private String registerEmailTitle = "邮箱验证码";
    private String registerEmailContent =
            "<h3>尊敬的MyPan用户你好，您的验证码是：<b style='color:skyblue'>%s,</b></h3><h3>" + (RedisConstants.EMAIL_CODE_TTL / 60) + "分钟后过期,请尽快在页面中输入以完成验证。</h3>";
    private Integer userInitTotalSpace = SystemConstants.USER_MAX_SPACE_MB; // 初始空间大小 MB

}
