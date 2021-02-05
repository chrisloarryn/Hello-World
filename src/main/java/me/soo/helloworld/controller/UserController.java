package me.soo.helloworld.controller;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import me.soo.helloworld.annotation.CurrentUser;
import me.soo.helloworld.exception.FileUploadException;
import me.soo.helloworld.exception.IncorrectUserInfoException;
import me.soo.helloworld.model.user.*;
import me.soo.helloworld.service.LoginService;
import me.soo.helloworld.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;


import static me.soo.helloworld.util.http.HttpResponses.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final LoginService loginService;

    @PostMapping("/signup")
    public ResponseEntity<Void> userSignUp(@Valid @RequestBody User user) {

        userService.userSignUp(user);

        return HTTP_RESPONSE_CREATED;
    }

    @GetMapping("/idcheck")
    public ResponseEntity<Void> isUserIdDuplicate(@RequestParam String userId) {

        if (userService.isUserIdDuplicate(userId)) {
            return HTTP_RESPONSE_CONFLICT;
        }

        return HTTP_RESPONSE_OK;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> userLogin(@Valid @RequestBody UserLoginRequest loginRequest) {

        try {
            User user = userService.getUser(loginRequest);
            loginService.login(user.getUserId());

            return HTTP_RESPONSE_OK;

        } catch (IncorrectUserInfoException e) {
            return HTTP_RESPONSE_NOT_FOUND;

        } catch (DuplicateRequestException e) {
            return HTTP_RESPONSE_UNAUTHORIZED;
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> userLogout() {

        loginService.logout();

        return HTTP_RESPONSE_NO_CONTENT;
    }

    @PutMapping("/account/password")
    public ResponseEntity<Void> userPasswordUpdate(@CurrentUser String userId,
                                                   @RequestBody @Valid UserPasswordRequest userPasswordRequest) {

        userService.userPasswordUpdate(userId, userPasswordRequest);
        loginService.logout();

        return HTTP_RESPONSE_OK;
    }

    @PutMapping("/account")
    public ResponseEntity<Void> userUpdate(@CurrentUser String userId,
                                           @RequestPart("profileImage") MultipartFile profileImage,
                                           UserUpdateRequest updateRequest) {

        try {
            userService.userUpdate(userId, profileImage, updateRequest);
            return HTTP_RESPONSE_OK;
        } catch (FileUploadException e) {
            return HTTP_RESPONSE_BAD_REQUEST;
        }
    }
}
