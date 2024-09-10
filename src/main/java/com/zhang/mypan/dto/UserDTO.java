package com.zhang.mypan.dto;

import com.zhang.mypan.config.AppConfig;
import com.zhang.mypan.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

@Data
@EqualsAndHashCode
@ToString
public class UserDTO {
    private String id;
    private String nickName;
    private String email;
    private String avatar;
    private Boolean isAdmin;
    private Long userUseSpace;
    private Long userTotalSpace;
    private Integer status;  // 0 正常 1 禁用

    public static UserDTO parseUserDTO(User user, AppConfig appConfig) {
        UserDTO userDTO = new UserDTO();
        userDTO.setIsAdmin(ArrayUtils.contains(appConfig.getAdmin(), user.getEmail()));
        userDTO.setId(user.getUserId());
        userDTO.setEmail(user.getEmail());
        userDTO.setAvatar(user.getQqAvatar());
        userDTO.setNickName(user.getNickName());
        userDTO.setUserUseSpace(user.getUseSpace());
        userDTO.setUserTotalSpace(user.getTotalSpace());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }

    public static User parseUser(UserDTO userDTO) {
        User user = new User();
        user.setUserId(userDTO.getId());
        user.setQqAvatar(userDTO.getAvatar());
        user.setNickName(userDTO.getNickName());
        user.setTotalSpace(userDTO.getUserTotalSpace());
        user.setUseSpace(userDTO.getUserUseSpace());
        return user;
    }

}
