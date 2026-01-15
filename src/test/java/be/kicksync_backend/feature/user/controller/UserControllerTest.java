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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import be.kicksync_backend.feature.token.RefreshTokenRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
@TestPropertySource(properties = "spring.cache.type=none")
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

    private User testUser;
    private String uniqueUsername;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        uniqueUsername = "user" + (System.nanoTime() % 1000000000);
        testUser = new User(uniqueUsername, passwordEncoder.encode("Password123!"));
        userRepository.saveAndFlush(testUser);
    }


    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() throws Exception {
        // given
        String signupUsername = "new" + (System.nanoTime() % 1000000000);
        UserSignupRequestDto requestDto = new UserSignupRequestDto(signupUsername, "Password123!");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("회원가입에 성공하였습니다."))
                .andExpect(jsonPath("$.data.username").value(signupUsername));

        // DB 상태 검증
        User foundUser = userRepository.findByUsername(signupUsername).orElse(null);
        assertNotNull(foundUser);
        assertTrue(passwordEncoder.matches("Password123!", foundUser.getPassword()));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 사용자 이름")
    void signupFail_duplicateUsername() throws Exception {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto(uniqueUsername, "NewPassword123!");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict());
    }

// ...

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccess() throws Exception {
        // given
        User user = userRepository.findByUsername(uniqueUsername).get();

        UserLoginRequestDto requestDto = new UserLoginRequestDto(uniqueUsername, "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.parse(responseBody).read("$.data.accessToken");

        // when
        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("로그아웃에 성공하였습니다."));

        // then
        assertTrue(userRepository.findByUsername(uniqueUsername).isPresent());
        assertFalse(refreshTokenRepository.findByUser(user).isPresent());
    }

    @Test
    @DisplayName("회원탈퇴 성공 테스트")
    void deleteAccountSuccess() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(uniqueUsername, "Password123!");
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
        assertFalse(userRepository.findByUsername(uniqueUsername).isPresent());
    }
}