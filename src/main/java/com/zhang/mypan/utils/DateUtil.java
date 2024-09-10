package com.zhang.mypan.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static Date addDay(Integer day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, day);
        return cal.getTime();
    }
}
