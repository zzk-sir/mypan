package com.zhang.mypan.enums;

public enum RegexEnum {
    NO("", "不校验"),
    N_NUMBER("^[0-9]*$", "数字"),
    N_LENGTH_NUMBER("^\\d{n}$", "n位的数字"),
    AT_LEAST_N_LENGTH_NUMBER("^\\d{n,}$", "至少n位的数字"),
    M_N_LENGTH_NUMBER("^\\d{m,n}$", "m-n位的数字"),
    ZERO_NONZERO_NUMBER("^(0|[1-9][0-9]*)$", "零和非零开头的数字"),
    NONZERO_DECIMAL("^([1-9][0-9]*)+(.[0-9]{1,2})?$", "非零开头的最多带两位小数的数字"),
    POS_NEG_DECIMAL("^(-)?\\d+(\\.\\d{1,2})?$", "带1-2位小数的正数或负数"),
    POS_NEG_NUM_DECIMAL("^(-|\\+)?\\d+(\\.\\d+)?$", "正数、负数、和小数"),
    POS_REAL_NUM("^([0-9]+(.[0-9]{2})?)?$", "有两位小数的正实数"),
    POS_REAL_NUM_1_3("^([0-9]+(.[0-9]{1,3})?)?$", "有1~3位小数的正实数"),
    POSITIVE_INT("^[1-9]\\d*$", "非零的正整数"),
    NEGATIVE_INT("^-[1-9]\\d*$", "非零的负整数"),
    NON_NEGATIVE_INT("^\\d+$", "非负整数"),
    NON_POSITIVE_INT("^-([1-9]\\d*|0)$", "非正整数"),
    NON_NEGATIVE_FLOAT("^\\d+(\\.\\d+)?$", "非负浮点数"),
    NON_POSITIVE_FLOAT("^(-\\d+(\\.\\d+)?)|(0+(.0+)?)$", "非正浮点数"),
    POS_FLOAT("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$", "正浮点数"),
    NEG_FLOAT("^-([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*)$", "负浮点数"),
    FLOAT("^(-?\\d+)(\\.\\d+)?$", "浮点数"),
    CHINESE("^[\\u4e00-\\u9fa5]{0,}$", "汉字"),
    ENGLISH_AND_NUMBER("^[A-Za-z0-9]+$", "英文和数字"),
    LENGTH_3_TO_20(".{3,20}", "长度为3-20的所有字符"),
    ENGLISH_LETTERS("^[A-Za-z]+$", "由26个英文字母组成的字符串"),
    UPPERCASE_LETTERS("^[A-Z]+$", "由26个大写英文字母组成的字符串"),
    LOWERCASE_LETTERS("^[a-z]+$", "由26个小写英文字母组成的字符串"),
    ENGLISH_NUMBER_UNDERSCORE("^[A-Za-z0-9_]+$", "由数字、26个英文字母或者下划线组成的字符串"),
    CHINESE_ENGLISH_NUMBER_UNDERSCORE("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", "中文、英文、数字包括下划线"),
    CHINESE_ENGLISH_NUMBER("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", "中文、英文、数字但不包括下划线等符号"),
    SPECIAL_CHARACTERS("[^%&',;=?$\"\\\\]+", "可以输入含有^%&',;=?$\"等字符"),
    NO_TILDE("[^~\\\\]+", "禁止输入含有~的字符"),
    IP("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)", "IP"),
    EMAIL("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", "邮箱"),
    DOMAIN("[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(/.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+/.?", "域名"),
    INTERNET_URL("[a-zA-z]+://[^\\s]*", "InternetURL"),
    PHONE_NUMBER("^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$", "手机号码"),
    TELEPHONE_NUMBER("^((\\d{3,4}-)|\\d{3.4}-)?\\d{7,8}$", "电话号码"),
    DOMESTIC_PHONE_NUMBER("\\d{3}-\\d{8}|\\d{4}-\\d{7}", "国内电话号码"),
    ID_CARD_NUMBER("^\\d{15}|\\d{18}$", "身份证号"),
    SHORT_ID_CARD_NUMBER("^([0-9]){7,18}(x|X)?$", "短身份证号码"),
    LEGAL_ACCOUNT("^[a-zA-Z][a-zA-Z0-9_]{4,15}$", "帐号是否合法"),
    PASSWORD("^[a-zA-Z]\\w{5,17}$", "密码"),
    STRONG_PASSWORD("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}$", "强密码"),
    DATE_FORMAT("^\\d{4}-\\d{1,2}-\\d{1,2}", "日期格式"),
    MONTHS_1_TO_12("^(0?[1-9]|1[0-2])$", "一年的12个月"),
    DAYS_1_TO_31("^((0?[1-9])|((1|2)[0-9])|30|31)$", "一个月的31天"),
    VAILD_FILENAME("^[^\\\\/:*?\"<>|]+(?:\\.[^\\\\/:*?\"<>|]{1,200})*$", "文件名校验", "文件名不能含有\\, /, :, *, ?, \", <, >, | 这些特殊字符，且长度不超过200");
    // ... 更多的正则表达式

    private final String regex;
    private final String desc;
    private final String errmsg;

    RegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
        this.errmsg = "参数错误";
    }

    RegexEnum(String regex, String desc, String errmsg) {
        this.regex = regex;
        this.desc = desc;
        this.errmsg = errmsg;
    }

    public String getRegex() {
        return this.regex;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getErrmsg() {
        return errmsg;
    }
}
