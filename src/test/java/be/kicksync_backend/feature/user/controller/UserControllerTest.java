package be.kicksync_backend.feature.user.controller;

import be.kicksync_backend.common.config.JwtAuthenticationFilter;
import be.kicksync_backend.common.config.SecurityConfig;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.dto.UserSignupRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() throws Exception {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto("testuser", "Password123!");
        User user = new User("testuser", "Password123!");
        UserResponseDto responseDto = new UserResponseDto(user);

        given(userService.signup(any(UserSignupRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value(ResponseText.USER_SIGNUP_SUCCESS.getMsg()))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("testuser", "Password123!");
        JwtResponseDto responseDto = new JwtResponseDto("accessToken", "refreshToken");

        given(userService.login(any(UserLoginRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value(ResponseText.USER_LOGIN_SUCCESS.getMsg()))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccess() throws Exception {
        // given
        User user = new User("testuser", "Password123!");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // when & then
        mockMvc.perform(post("/api/users/logout")
                        .with(csrf())
                        .with(user(userDetails))
                        .header("Authorization", "Bearer accessToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value(ResponseText.LOGOUT_SUCCESS.getMsg()));

        // Service 메소드 호출 검증
        verify(userService).logout(any(UserDetailsImpl.class), anyString());
    }

    @Test
    @DisplayName("회원탈퇴 성공 테스트")
    void deleteAccountSuccess() throws Exception {
        // given
        User user = new User("testuser", "Password123!");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // when & then
        mockMvc.perform(delete("/api/users/account")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value(ResponseText.DELETE_ACCOUNT_SUCCESS.getMsg()));

        // Service 메소드 호출 검증
        verify(userService).deleteAccount(any(UserDetailsImpl.class));
    }
}
