package me.soo.helloworld.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.soo.helloworld.model.user.User;
import me.soo.helloworld.model.user.UserLoginInfo;
import me.soo.helloworld.repository.UserRepository;
import me.soo.helloworld.service.UserLoginTest;
import me.soo.helloworld.service.UserService;
import me.soo.helloworld.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerUnitTest {
    User testUser;
    UserLoginInfo testUserLoginInfo;

    MockHttpSession mockHttpSession;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .userId("gomsu1045")
                .password("Gomsu1045!0$%")
                .email("test@test.com")
                .gender("Male")
                .birthday(Date.valueOf("1993-09-25"))
                .originCountry("South Korea")
                .livingCountry("United Kingdom")
                .livingTown("Newcastle Upon Tyne")
                .aboutMe("Hello, I'd love to make great friends here")
                .build();

        testUserLoginInfo = new UserLoginInfo("gomsu1045", "Gomsu1045!0$%");
        mockHttpSession = new MockHttpSession();
    }

    @Test
    @DisplayName("회원가입 요청에 성공시 HTTP Status Code 201(Created)를 리턴합니다.")
    public void userSignUpControllerTest() throws Exception {
        String content = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(userService).userSignUp(any(User.class));
    }

    @Test
    @DisplayName("이미 등록되어 있는 아이디일 경우 Http Status Code 409(Conflict)를 리턴합니다.")
    public void userIdDuplicateTestSuccess() throws Exception {
        mockMvc.perform(get("/users/idcheck")
                .param("userId", "Soo"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).isUserIdDuplicate("Soo");
    }

    @Test
    @DisplayName("등록되어 있는 ID가 아닌 경우 Http Status Code 200(Ok)를 리턴합니다.")
    public void userIdDuplicateTestFail() throws Exception {
        when(userService.isUserIdDuplicate("Soo")).thenReturn(true);

        mockMvc.perform(get("/users/idcheck")
                .param("userId", "Soo"))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(userService).isUserIdDuplicate("Soo");
    }

    @Test
    @DisplayName("DB에 등록된 정보와 일치하는 정보를 입력하면 로그인에 성공하고 Http Status Code 200(Ok)를 리턴합니다.")
    public void userLoginTestSuccess() throws Exception {
        String content = objectMapper.writeValueAsString(testUserLoginInfo);

        mockMvc.perform(post("/users/login")
                .content(content)
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).loginRequest(testUserLoginInfo, mockHttpSession);
    }


    @Test
    @DisplayName("이미 로그인된 회원의 경우 로그인에 실패하며 Http Status Code 401(Unauthorized)를 리턴합니다.")
    public void userLoginTestFailWithAlreadyLoginUser() throws Exception {
        mockHttpSession.setAttribute("userId", testUserLoginInfo.getUserId());

        String content = objectMapper.writeValueAsString(testUserLoginInfo);

        doThrow(new DuplicateKeyException("해당 유저는 이미 로그인 되어 있습니다."))
                .when(userService).loginRequest(testUserLoginInfo, mockHttpSession);

        mockMvc.perform(post("/users/login")
                .content(content)
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService).loginRequest(testUserLoginInfo, mockHttpSession);
        mockHttpSession.invalidate();
    }


    @Test
    @DisplayName("등록되지 않은 사용자의 경우 로그인에 실패하며 Http Status Code 401(Unauthorized)를 리턴합니다.")
    public void userLoginTestFailWithNoSuchUser() throws Exception {
        UserLoginInfo wrongTestUser = new UserLoginInfo("Soo", "Bakery");
        String contentWrongTestUser = objectMapper.writeValueAsString(wrongTestUser);

        doThrow(new IllegalArgumentException("해당 유저는 존재하지 않습니다."))
                .when(userService).loginRequest(wrongTestUser, mockHttpSession);

        mockMvc.perform(post("/users/login")
                .content(contentWrongTestUser)
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService).loginRequest(wrongTestUser, mockHttpSession);
    }

    @Test
    @DisplayName("로그아웃이 완료되면 Http Status Code 204(No Content)를 리턴합니다.")
    public void userLogoutTest() throws Exception {

        mockMvc.perform(get("/users/logout"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService).logoutRequest(any(MockHttpSession.class));
    }
}