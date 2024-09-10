package com.zhang.mypan.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import com.zhang.mypan.utils.SystemConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer KaptchaProducer() {
        Properties kaptchaProperties = new Properties();
        kaptchaProperties.put("kaptcha.border", "no");
        kaptchaProperties.put("kaptcha.textproducer.char.length", String.valueOf(SystemConstants.IMG_CODE_LEN));
        kaptchaProperties.put("kaptcha.image.height", "50");
        kaptchaProperties.put("kaptcha.image.width", "150");

        kaptchaProperties.put("kaptcha.background.clear.from", "white");
        kaptchaProperties.put("kaptcha.background.clear.to", "black");
        kaptchaProperties.put("kaptcha.noise.color", "200,200,200");
        kaptchaProperties.put("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.ShadowGimpy");
        kaptchaProperties.put("kaptcha.textproducer.font.color", "black");
        kaptchaProperties.put("kaptcha.textproducer.font.size", "40");
        kaptchaProperties.put("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        //kaptchaProperties.put("kaptcha.noise.impl","com.google.code.kaptcha.impl.DefaultNoise");
        kaptchaProperties.put("kaptcha.textproducer.char.string", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        Config config = new Config(kaptchaProperties);
        return config.getProducerImpl();
    }
}