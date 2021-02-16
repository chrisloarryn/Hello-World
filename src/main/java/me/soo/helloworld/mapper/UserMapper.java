package me.soo.helloworld.mapper;

import me.soo.helloworld.model.file.FileData;
import me.soo.helloworld.model.user.User;
import me.soo.helloworld.model.user.UserUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.io.File;

@Mapper
public interface UserMapper {

    public void insertUser(User user);

    public boolean isUserIdDuplicate(String userId);

    public User getUserById(String userId);

    public FileData getUserProfileImageById(String userId);

    public void updateUserPassword(@Param("userId") String userId, @Param("password") String password);

    public void updateUserProfileImage(@Param("userId") String userId, @Param("profileImage") FileData newProfileImage);

    public void updateUserInfo(@Param("userId") String userId, @Param("updateRequest") UserUpdateRequest updateRequest);

    public String getUserPasswordById(String userId);

    public void deleteUser(String userId);
}
