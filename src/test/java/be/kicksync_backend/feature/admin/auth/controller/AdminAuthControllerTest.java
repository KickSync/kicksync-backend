package be.kicksync_backend.feature.admin.auth.controller;

import be.kicksync_backend.common.config.SecurityConfig;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.security.jwt.JwtAccessDeniedHandler;
import be.kicksync_backend.common.security.jwt.JwtAuthenticationEntryPoint;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.admin.auth.dto.AdminLoginRequestDto;
import be.kicksync_backend.feature.admin.auth.dto.AdminSignupRequestDto;
import be.kicksync_backend.feature.admin.auth.service.AdminAuthService;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.entity.Role;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminAuthController.class)
@Import(SecurityConfig.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminAuthService adminAuthService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RedisTokenService redisTokenService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    @DisplayName("관리자 회원가입 성공 테스트")
    void adminSignupSuccess() throws Exception {
        // given
        AdminSignupRequestDto requestDto = new AdminSignupRequestDto("admin123", "Password123!", "ADMIN_SECRET_KEY");
        User user = new User("admin123", "encodedPassword", Role.ADMIN);
        UserResponseDto responseDto = new UserResponseDto(user);

        given(adminAuthService.signup(any(AdminSignupRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/admin/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("관리자 회원가입 성공"))
                .andExpect(jsonPath("$.data.username").value("admin123"));
    }

    @Test
    @DisplayName("관리자 로그인 성공 테스트")
    void adminLoginSuccess() throws Exception {
        // given
        AdminLoginRequestDto requestDto = new AdminLoginRequestDto("admin123", "Password123!");
        JwtResponseDto responseDto = new JwtResponseDto("accessToken", "refreshToken");

        given(adminAuthService.login(any(AdminLoginRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/admin/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("관리자 로그인 성공"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
    }
}
