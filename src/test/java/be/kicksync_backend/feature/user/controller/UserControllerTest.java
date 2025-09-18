package be.kicksync_backend.feature.user.controller;

import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;
import be.kicksync_backend.feature.user.dto.UserSignupRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import be.kicksync_backend.feature.user.repository.RefreshTokenRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import be.kicksync_backend.feature.token.RefreshTokenService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User testUser = new User("loginUser", passwordEncoder.encode("password123"));
        userRepository.save(testUser);
    }


    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() throws Exception {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto("testuser", "password123!");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("회원가입에 성공하였습니다."))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // DB 상태 검증
        User foundUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(foundUser);
        assertTrue(passwordEncoder.matches("password123!", foundUser.getPassword()));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 사용자 이름")
    void signupFail_duplicateUsername() throws Exception {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto("loginUser", "newPassword");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("loginUser", "password123");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("로그인에 성공하였습니다."))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFail_wrongPassword() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("loginUser", "wrongPassword");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccess() throws Exception {
        // given
        User user = userRepository.findByUsername("loginUser").get();
        refreshTokenRepository.deleteByUser(user);

        UserLoginRequestDto requestDto = new UserLoginRequestDto("loginUser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.parse(responseBody).read("$.data.accessToken");

        assertTrue(refreshTokenRepository.findByUser(user).isPresent());

        // when
        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("로그아웃에 성공하였습니다."));

        // then
        assertTrue(userRepository.findByUsername("loginUser").isPresent());
        assertFalse(refreshTokenRepository.findByUser(user).isPresent());
    }

    @Test
    @DisplayName("회원탈퇴 성공 테스트")
    void deleteAccountSuccess() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("loginUser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.parse(responseBody).read("$.data.accessToken");

        // when
        mockMvc.perform(delete("/api/users/account")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원탈퇴에 성공하였습니다."));

        // then
        assertFalse(userRepository.findByUsername("loginUser").isPresent());
    }
}