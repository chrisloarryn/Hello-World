package me.soo.helloworld.service;

import lombok.RequiredArgsConstructor;
import me.soo.helloworld.exception.FileUploadException;
import me.soo.helloworld.exception.IncorrectUserInfoException;
import me.soo.helloworld.model.file.FileData;
import me.soo.helloworld.model.user.*;
import me.soo.helloworld.repository.UserRepository;
import me.soo.helloworld.util.encoder.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final FileService fileService;

    private final PasswordEncoder passwordEncoder;

    public void userSignUp(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        userRepository.insertUser(user.buildUserWithEncodedPassword(encodedPassword));
    }

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.isUserIdDuplicate(userId);
    }

    public User getUser(UserLoginRequest loginRequest) {
        User user = userRepository.getUserById(loginRequest.getUserId());

        if (user == null) {
            throw new IncorrectUserInfoException("해당 유저의 정보는 존재하지 않습니다. 아이디를 다시 확인해주세요.");
        }

        boolean isMatchingPassword = passwordEncoder.isMatch(loginRequest.getPassword(), user.getPassword());

        if (!isMatchingPassword) {
            throw new IncorrectUserInfoException("입력하신 비밀번호가 일치하지 않습니다. 비밀번호를 다시 한 번 확인해주세요.");
        }

        return user;
    }

    public void userPasswordUpdate(String currentUserId, UserPasswordRequest userPasswordRequest) {
        String encodedPassword = passwordEncoder.encode(userPasswordRequest.getNewPassword());
        userRepository.updateUserPassword(currentUserId, encodedPassword);
    }

    public void userUpdate(String userId, MultipartFile profileImage, UserUpdateRequest updateRequest) {

        try {
            FileData oldProfileImage = userRepository.getUserProfileImageById(userId);

            if (oldProfileImage != null) {
                fileService.deleteFile(oldProfileImage);
            }

            FileData newProfileImage = fileService.uploadFile(profileImage, userId);
            userRepository.updateUser(userId, updateRequest, newProfileImage);
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드에 실패하였습니다. ", e.getCause());
        }
    }


}
