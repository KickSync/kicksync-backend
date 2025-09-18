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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}