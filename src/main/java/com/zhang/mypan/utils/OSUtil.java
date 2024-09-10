package com.zhang.mypan.utils;

import com.zhang.mypan.enums.OSEnums;

public class OSUtil {
    public static OSEnums getCurOperaSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OSEnums.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return OSEnums.LINUX;
        } else if (os.contains("mac")) {
            return OSEnums.MAC;
        } else {
            return OSEnums.OTHER;
        }
    }

}
