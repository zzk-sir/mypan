package com.zhang.mypan.dto;

public class QQInfoDto {
    // 返回码
    private int ret;
    // 错误信息提示
    private String msg;
    // 是否有数据丢失
    private int is_lost;
    // 用户在QQ空间的昵称
    private String nickname;
    // QQ空间头像URL（30×30像素）
    private String figureurl;
    // QQ空间头像URL（50×50像素）
    private String figureurl_1;
    // QQ空间头像URL（100×100像素）
    private String figureurl_2;
    // QQ头像URL（40×40像素）
    private String figureurl_qq_1;
    // QQ头像URL（100×100像素）
    private String figureurl_qq_2;
    // 性别
    private String gender;
    // 性别类型
    private int gender_type;
    // 省份
    private String province;
    // 城市
    private String city;
    // 年份
    private String year;
    // 星座
    private String constellation;
    // 是否为黄钻用户
    private int is_yellow_vip;
    // 黄钻等级
    private int yellow_vip_level;
    // 是否为年费黄钻用户
    private int is_yellow_year_vip;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getIs_lost() {
        return is_lost;
    }

    public void setIs_lost(int is_lost) {
        this.is_lost = is_lost;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFigureurl() {
        return figureurl;
    }

    public void setFigureurl(String figureurl) {
        this.figureurl = figureurl;
    }

    public String getFigureurl_1() {
        return figureurl_1;
    }

    public void setFigureurl_1(String figureurl_1) {
        this.figureurl_1 = figureurl_1;
    }

    public String getFigureurl_2() {
        return figureurl_2;
    }

    public void setFigureurl_2(String figureurl_2) {
        this.figureurl_2 = figureurl_2;
    }

    public String getFigureurl_qq_1() {
        return figureurl_qq_1;
    }

    public void setFigureurl_qq_1(String figureurl_qq_1) {
        this.figureurl_qq_1 = figureurl_qq_1;
    }

    public String getFigureurl_qq_2() {
        return figureurl_qq_2;
    }

    public void setFigureurl_qq_2(String figureurl_qq_2) {
        this.figureurl_qq_2 = figureurl_qq_2;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getGender_type() {
        return gender_type;
    }

    public void setGender_type(int gender_type) {
        this.gender_type = gender_type;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getConstellation() {
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public int getIs_yellow_vip() {
        return is_yellow_vip;
    }

    public void setIs_yellow_vip(int is_yellow_vip) {
        this.is_yellow_vip = is_yellow_vip;
    }

    public int getYellow_vip_level() {
        return yellow_vip_level;
    }

    public void setYellow_vip_level(int yellow_vip_level) {
        this.yellow_vip_level = yellow_vip_level;
    }

    public int getIs_yellow_year_vip() {
        return is_yellow_year_vip;
    }

    public void setIs_yellow_year_vip(int is_yellow_year_vip) {
        this.is_yellow_year_vip = is_yellow_year_vip;
    }
}
