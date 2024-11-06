package com.zhang.mypan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.code.kaptcha.Producer;
import com.zhang.mypan.config.AppConfig;
import com.zhang.mypan.dto.*;
import com.zhang.mypan.entity.User;
import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.enums.UserStatus;
import com.zhang.mypan.exception.MyPanException;
import com.zhang.mypan.service.FileInfoService;
import com.zhang.mypan.service.UserService;
import com.zhang.mypan.mapper.UserMapper;
import com.zhang.mypan.utils.*;
import com.zhang.mypan.vo.PaginationResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 86180
 * @description 针对表【user_info】的数据库操作Service实现
 * @createDate 2024-03-16 20:39:37
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private Producer kaptchaProducer;
    private RedisUtil redisUtil;
    private AppConfig appConfig;
    private FileInfoService fileInfoService;

    @Override
    public void getCode(HttpServletRequest request, HttpServletResponse response) {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        // 创建验证码
        String capText = kaptchaProducer.createText();
        log.info("验证码=== {}", capText);
        // 存入session中
        request.getSession().setAttribute(SessionConstants.IMAGE_CODE, capText);

        // 将验证码字符串转化为图片
        BufferedImage bi = kaptchaProducer.createImage(capText);

        try {
            ImageIO.write(bi, "jpg", response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result register(String email, String nickName, String password, String emailCode) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        // 检验用户是否存在
        wrapper
                .or(w -> w.eq("email", email))
                .or(w -> w.eq("nick_name", nickName));
        long UserCount = count(wrapper);
        if (UserCount > 0) {
            return Result.fail("邮箱已存在", CodeEnum.CONFILCT);
        }
        // 校验邮箱验证码 在redis中
        Result BAD_REQUEST = checkEmailCode(email, emailCode);
        if (BAD_REQUEST != null) return BAD_REQUEST;
        // 生成随机userID
        String userId = RandomUtil.randomString(SystemConstants.USER_ID_LEN);
        SysSettingDto sysSettingDto = redisUtil.get(RedisConstants.SYS_SETTING_KEY, SysSettingDto.class);
        // 添加到数据库
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setNickName(nickName);
        user.setUseSpace(0L);
        if (sysSettingDto != null) {
            user.setTotalSpace(sysSettingDto.getUserInitTotalSpace() * 1024 * 1024L);
        } else {
            user.setTotalSpace(SystemConstants.USER_MAX_SPACE_MB * 1024 * 1024L);
        }
        user.setCreateTime(new Date());
        save(user);
        // 保存到UserHolder 注册不需要保存，因为注册好后用户还要登录
        // UserHolder.saveUser(UserDTO.parseUserDTO(user));
        return Result.ok("注册成功,请登录");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result login(String email, String password) {
        // 校验用户是否存在
        User user = query().eq("email", email)
                // 前端登录注册都需要将密码加密
                .eq("password", DigestUtil.md5Hex(password))
                .one();
        if (ObjectUtil.isNull(user)) {
            throw new MyPanException("账号或密码错误", CodeEnum.BAD_REQUEST);
        }
        if (user.getStatus().equals(UserStatus.DEL.getStatus())) {
            return Result.fail("当前用户不可用,请联系管理员", CodeEnum.USERNOTUSE);
        }
        // 更新user信息
        user.setLastLoginTime(new Date());
        final boolean success = this.updateById(user);
        if (!success) {
            // 更新失败
            log.error("更新失败");
            return Result.fail("更新失败", CodeEnum.INTERNAL_SERVER_ERROR);
        }
        // 映射为userDTO
        final UserDTO userDTO = UserDTO.parseUserDTO(user, appConfig);
        // 在登录时设置使用空间大小，这样在redis中是初始登录的空间大小，UserHolder中是实时编号的大小
        userDTO.setUserUseSpace(fileInfoService.getUseSpace(user.getUserId()));
        // 保存到UserHolder
        UserHolder.saveUser(userDTO);
        // 创建jwtToken
        String jwtToken = JwtUtil.createJWT(userDTO.getId());
        String key = RedisConstants.USER_LOGIN_KEY + userDTO.getId();
        // 存入redis
        boolean success1 = redisUtil.hmsetFromObj(key, userDTO);
        // 设置 TTL
        redisUtil.expire(key, RedisConstants.USER_LOGIN_TTL);
        if (!success1) {
            return Result.fail("登录失败", CodeEnum.INTERNAL_SERVER_ERROR);
        }
        System.out.println("登录的用户信息:" + userDTO);
        // 返回token
        return Result.ok(jwtToken);
    }

    @Override
    public Result resetPwd(String email, String password, String emailCode) {
        // 检测邮箱验证码是否正确
        Result BAD_REQUEST = checkEmailCode(email, emailCode);
        if (BAD_REQUEST != null) return BAD_REQUEST;
        // 邮箱验证码正确
        // 更新密码
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        boolean update = update()
                .set("password", DigestUtil.md5Hex(password))
                .eq("email", email)
                .update();
        if (!update) {
            // 更新失败
            return Result.fail("更新密码失败", CodeEnum.INTERNAL_SERVER_ERROR);
        }
        return Result.ok("重置密码成功");
    }

    @Override
    public void getAvatarIcon(HttpServletResponse response, String userId) {
        String basePath = appConfig.getProjectFolder();
        // redis 中获取是否存在该用户ID
        String key = RedisConstants.USER_LOGIN_KEY + userId;
        UserDTO userDTO = redisUtil.hmgetToObj(key, new UserDTO());
        if (userDTO == null) {
            // redis 中不存在
            // 从数据库中检测是否存在该用户ID
            final User user = getById(userId);
            if (user == null) {
                // key不存在
                throw new MyPanException(CodeEnum.BAD_REQUEST);
            }
            userDTO = UserDTO.parseUserDTO(user, appConfig);
        }
        // 用户头像文件夹路径
        // 用户头像
        File folder = new File(basePath + SystemConstants.FILE_FOLDER_AVATAR + userId);
        if (!folder.exists()) {
            // 用户头像文件夹路径不存在创建
            folder.mkdirs();
        }
        String avatarIconPath = null;
        if (userDTO.getAvatar() == null) {
            // 没有 使用默认 并写入redis
            File avatarIconFile = FileUtil.getRandomImgOfFolder(basePath + SystemConstants.FILE_FOLDER_AVATAR_DEFAULT);
            if (avatarIconFile == null) {
                throw new MyPanException("获取头像失败", CodeEnum.NOT_FOUND);
            }
            avatarIconPath = avatarIconFile.getAbsolutePath();
            // 存入/userId{default}/imageName
            File file = new File(basePath + SystemConstants.FILE_FOLDER_AVATAR);
            //

            userDTO.setAvatar(avatarIconPath.substring(file.getAbsolutePath().length()));
            // 存入redis
            boolean success = redisUtil.hmsetFromObj(key, userDTO);
            if (!success) throw new MyPanException();
            // 更新到数据库 头像信息
            updateById(UserDTO.parseUser(userDTO));
        } else {
            // 有
            avatarIconPath = basePath + SystemConstants.FILE_FOLDER_AVATAR + userDTO.getAvatar();
        }
        // 相应给浏览器
        // 获取图片类型
        String imgType = FileUtil.getTypeByFilePath(avatarIconPath);
        response.setContentType("image/" + imgType);
        FileUtil.responseFile(response, avatarIconPath);
    }

    @Override
    public Result getUserInfo() {
        // 从UserHolder中获取
        UserDTO user = UserHolder.getUser();
        // user.setUserUseSpace(fileInfoService.getUseSpace(user.getId()));
        return Result.ok(user);
    }

    @Override
    public Result getUserSpace() {

        // 从userHolder中获取
        UserDTO user = UserHolder.getUser();
        UseSpaceDto useSpaceDto = new UseSpaceDto();
        useSpaceDto.setUseSpace(user.getUserUseSpace());
        useSpaceDto.setTotalSpace(user.getUserTotalSpace());
        return Result.ok(useSpaceDto);
    }

    @Override
    public Result logout() {
        final String id = UserHolder.getUser().getId();
        // 删除redis中的数据
        String key = RedisConstants.USER_LOGIN_KEY + id;
        redisUtil.del(key);
        // 删除ThreadLocal中的数据
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result updateUserAvatar(MultipartFile avatar) {

        // 获取当前用户
        UserDTO user = UserHolder.getUser();
        String userId = user.getId();
        // 更新用户头像目录下的用户头像
        String basePath = appConfig.getProjectFolder();
        String avatarIconPath = basePath + SystemConstants.FILE_FOLDER_AVATAR + userId;
        File avatarIconFile = new File(avatarIconPath);
        if (!avatarIconFile.exists()) {
            // 如果不存在则创建目录
            avatarIconFile.mkdirs();
        }
        File[] files = avatarIconFile.listFiles();
        if (files != null) {
            for (File file : files) {
                // 删除之前的头像文件
                file.delete();
            }
        }
        // 格式 .xxx
        //String suffix = FileUtil.getSuffixFromContentType(avatar.getContentType());
        // 相对路径 avatar.getOriginalFilename()
        String avatarName = "/" + avatar.getOriginalFilename();

        File avatarIcon = new File(avatarIconPath + avatarName);
        try {
            avatar.transferTo(avatarIcon);
            // 防止用户修改文件后缀上传
            if (!FileUtil.isImage(avatarIcon)) {
                avatarIcon.delete();
                throw new MyPanException("文件格式错误", CodeEnum.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            throw new MyPanException("头像上传失败", CodeEnum.INTERNAL_SERVER_ERROR);
        }
        // 更新 user
        // /userId/userId.xxx
        user.setAvatar("/" + userId + avatarName);
        // 更新ThreadLocal
        UserHolder.saveUser(user);
        // 更新redis
        String key = RedisConstants.USER_LOGIN_KEY + userId;
        boolean success = redisUtil.hmsetFromObj(key, user);
        if (!success) throw new RuntimeException();
        // 更新数据库
        updateById(UserDTO.parseUser(user));
        return Result.ok("修改成功");
    }

    @Override
    public Result qqlogin(HttpSession session, String callbackUrl) {
        // 生成状态码
        String state = RandomUtil.randomNumbers(SystemConstants.RANDOM_QQ_STATE_LEN);
        // 存入session
        session.setAttribute(state, callbackUrl);
        String url = null;
        try {
            url = String.format(appConfig.getQqUrlAuthrization(), appConfig.getQqUrlOpenId(),
                    URLEncoder.encode(appConfig.getQqUrlRedirect(), "UTF-8"), state);
        } catch (UnsupportedEncodingException e) {
            log.error("qq登录获取url失败", e);
            throw new MyPanException();
        }
        return Result.ok(url);
    }

    @Override
    public Result qqloginCollback(HttpSession session, String code, String state) {
        Map<String, Object> result = new HashMap<>();
        UserDTO userDTO = qqlogin(code);
        result.put("callbackUrl", session.getAttribute(state));
        result.put("userInfo", userDTO);
        // 将userDTO存入redis
        boolean success = redisUtil.hmsetFromObj(RedisConstants.USER_LOGIN_KEY, userDTO);
        if (!success) {
            log.error("qq登录存入reids失败");
            throw new MyPanException();
        }
        // 返回结果
        return Result.ok(result);
    }

    /**
     * 设置用户空间大小
     *
     * @param userId
     * @param useSpace
     * @param totalSpace
     */
    @Override
    public void setUserSpace(String userId, Long useSpace, Long totalSpace) {
        Integer res = getBaseMapper().setUserSpace(userId, useSpace, totalSpace);
        if (res == 0) {
            throw new MyPanException(CodeEnum.SPACE_OVERFLOW);
        }
        // 更新用户空间大小
        UserDTO user = redisUtil.hmgetToObj(RedisConstants.USER_LOGIN_KEY + userId, new UserDTO());
        // redis 没有数据
        if (user == null) {
            log.error(this.getClass().getName() + ":从redis获取用户失败");
            return;
        }
        final Long userUseSpace = user.getUserUseSpace();
        final Long userTotalSpace = user.getUserTotalSpace();
        if (useSpace != null) user.setUserUseSpace(useSpace + userUseSpace);
        if (totalSpace != null) user.setUserTotalSpace(totalSpace + userTotalSpace);
        // 更新redis数据
        String key = RedisConstants.USER_LOGIN_KEY + user.getId();
        redisUtil.hmsetFromObj(key, user);
    }


    @Override
    public Result updatePassword(String password) {
        // 获取当前用户
        UserDTO userDTO = UserHolder.getUser();
        User user = UserDTO.parseUser(userDTO);
        user.setPassword(DigestUtil.md5Hex(password));
        // 更新数据库
        boolean success = updateById(user);
        if (!success) {
            throw new MyPanException("修改密码失败");
        }
        return Result.ok("修改成功");
    }

    @Override
    public Result loadUserList(Integer pageNo, Integer pageSize, Integer status, String nickNameFuzzy) {
        final LambdaQueryChainWrapper<User> wrapper = lambdaQuery();
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (nickNameFuzzy != null) {
            wrapper.like(User::getNickName, nickNameFuzzy);
        }
        final Page<User> page = wrapper.page(new Page<User>(pageNo, pageSize));
        final List<UserInfo> userList = page.getRecords().stream().map(user -> new UserInfo(user, appConfig)).collect(Collectors.toList());
        return Result.ok(new PaginationResultVO<UserInfo>(page.getTotal(),
                page.getSize(), page.getCurrent(), page.getPages(), userList));
    }

    @Override
    public Result updateUserStatus(String userId, Integer status) {
        final boolean success = lambdaUpdate().eq(User::getUserId, userId)
                .set(User::getStatus, status).update();
        if (!success) return Result.fail("状态更新失败", CodeEnum.BAD_REQUEST);
        return Result.ok("状态更新成功");
    }

    @Override
    public void deleteUserSpace(String userId) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(User::getUseSpace, 0L)
                .eq(User::getUserId, userId);
        update(wrapper);
    }


    private UserDTO qqlogin(String code) {
        // 核心代码，与qq交互
        // 1. 通过回调code 获取accessToken
        String token = getQQAccessToken(code);
        // 2. 获取qqopenId
        String qqOpenId = getQQOpenId(code);
        // 3. 在数据库中获取用户信息
        User qquser = query().eq("qq_openid", qqOpenId).one();
        String avatar = null;
        if (qquser == null) {
            // 3.1. 没有 获取qqInfo
            QQInfoDto qqInfoDto = getQQinfo(token, qqOpenId);
            qquser = new User();
            String nickname = qqInfoDto.getNickname();
            // 检测qq用户昵称，防止其过长，数据库装不下
            nickname = nickname.length() > SystemConstants.NICKNAME_MAX_LEN
                    ? nickname.substring(0, SystemConstants.NICKNAME_MAX_LEN)
                    : nickname;
            avatar = StringUtils.isEmpty(qqInfoDto.getFigureurl_qq_2())
                    ? qqInfoDto.getFigureurl_qq_1()
                    : qqInfoDto.getFigureurl_qq_2();
            Date curDate = new Date();

            SysSettingDto sysSettingDto = redisUtil.get(RedisConstants.SYS_SETTING_KEY, SysSettingDto.class);
            qquser.setQqOpenId(qqOpenId);
            qquser.setCreateTime(curDate);
            qquser.setNickName(nickname);
            qquser.setQqAvatar(avatar);
            qquser.setUserId(RandomUtil.randomString(SystemConstants.USER_ID_LEN));
            qquser.setLastLoginTime(curDate);
            qquser.setUseSpace(0L);
            if (sysSettingDto != null) {
                qquser.setTotalSpace(sysSettingDto.getUserInitTotalSpace() * 1024L * 1024L);
            } else {
                qquser.setTotalSpace(SystemConstants.USER_MAX_SPACE_MB * 1024L * 1024L);
            }
            // 3.2. 保存用户信息
            save(qquser);
        }
        // 4. 有 更新
        UserDTO userDTO = UserDTO.parseUserDTO(qquser, appConfig);
        // 检测是否为管理员
        userDTO.setIsAdmin(ArrayUtils.contains(appConfig.getAdmin(),
                qquser.getEmail() == null ? "" : qquser.getEmail()));
        // 保存到ThreadLocal
        userDTO.setUserUseSpace(fileInfoService.getUseSpace(qquser.getUserId()));
        UserHolder.saveUser(userDTO);

        return userDTO;
    }

    /**
     * 获取qqopenId
     *
     * @param code
     * @return
     */
    private String getQQOpenId(String code) {
        String url = String.format(appConfig.getQqUrlOpenId(), code);
        String openIDResult = OKHttpUtils.getRequest(url);
        String tmpJson = getQQResp(openIDResult);
        if (tmpJson == null) {
            log.error("qq登录获取openID失败{}", openIDResult);
            throw new MyPanException();
        }
        Map jsonData = JSONObject.parseObject(tmpJson, Map.class);

        if (jsonData == null || jsonData.containsKey(SystemConstants.VIEW_OBJ_RESULT_KEY)) {
            log.error("掉qq接口获取openId十八{}", jsonData);
            throw new MyPanException();
        }
        return String.valueOf(jsonData.get("openid"));
    }

    /**
     * 获取json结果
     *
     * @param result
     * @return
     */
    private String getQQResp(String result) {
        // 解析json
        if (StringUtils.isNotBlank(result)) {
            if (result.contains("callback")) {
                int start = result.indexOf("(");
                int end = result.lastIndexOf(")");
                return result.substring(start + 1, end - 1);
            }
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @param qqOpenId
     * @return
     */
    private QQInfoDto getQQinfo(String accessToken, String qqOpenId) {
        String url = String.format(appConfig.getQqUrlUserInfo(), appConfig.getQqAppId(), qqOpenId);
        String response = OKHttpUtils.getRequest(url);
        if (StringUtils.isNotBlank(response)) {
            QQInfoDto qqInfoDto = JSONObject.parseObject(response, QQInfoDto.class);
            if (qqInfoDto.getRet() != 0) {
                log.error("qq登录获取用户信息失败{}", response);
                throw new MyPanException();
            }
            return qqInfoDto;
        }
        log.error("调qq接口获取用户信息异常");
        throw new MyPanException();
    }

    /**
     * 获取用户token
     *
     * @param code
     * @return
     */
    private String getQQAccessToken(String code) {
        /**
         * 返回值是字符串 access_token=*&expires_in=777600&refresh_token=*
         * 错误返回callback(UcWebContents.VIEW_OBJ_RESULT_KEY:111,error_description:"error msg")
         */
        String accessToken = null;
        String url = null;

        try {
            url = String.format(appConfig.getQqUrlAccessToken(), appConfig.getQqAppId(),
                    appConfig.getQqAppKey(), code, URLEncoder.encode(appConfig.getQqUrlRedirect(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("encode失败", e);
            throw new MyPanException();
        }
        String tokenResult = OKHttpUtils.getRequest(url);
        if (tokenResult.contains(SystemConstants.VIEW_OBJ_RESULT_KEY)) {
            log.error("获取qqToken失败{}", tokenResult);
            throw new MyPanException();
        }
        String[] params = tokenResult.split("&");
        for (String param : params) {
            if (param.contains("access_token")) {
                accessToken = param.split("=")[1];
                break;
            }
        }
        return accessToken;
    }

    /**
     * 检测邮箱验证码是否正确
     *
     * @param email
     * @param emailCode
     * @return
     */
    private Result checkEmailCode(String email, String emailCode) {
        String key = RedisConstants.EMAIL_CODE_KEY + email;
        final Object emailcode = redisUtil.get(key);

        if (emailcode == null) {
            // 邮箱验证码为空
            return Result.fail("邮箱验证码错误或验证码已过期", CodeEnum.BAD_REQUEST);
        }
        String realEmailCode = (String) emailcode;
        if (!realEmailCode.equals(emailCode)) {
            // 邮箱验证码错误
            return Result.fail("邮箱验证码错误", CodeEnum.BAD_REQUEST);
        }
        // 检验成功
        delEmailCode(emailCode,email);

        return null;
    }

    private void delEmailCode(String emailCode, String email) {
        final String emailCodeKey = RedisConstants.EMAIL_CODE_KEY + email;
        final String emailCodeSetKey = RedisConstants.EMAIL_CODE_SET+emailCode;
        // 删除邮箱验证码 // 删除验证码放重复标记
        System.out.println(emailCodeKey+","+emailCodeSetKey);
        redisUtil.del(emailCodeKey,emailCodeSetKey);
    }

    @Autowired
    public void setCaptchaProducer(Producer kaptchaProducer) {
        this.kaptchaProducer = kaptchaProducer;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }
}




