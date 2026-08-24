package be.kicksync_backend.feature.admin.product.controller;

import be.kicksync_backend.common.config.SecurityConfig;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.security.jwt.JwtAccessDeniedHandler;
import be.kicksync_backend.common.security.jwt.JwtAuthenticationEntryPoint;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.admin.product.service.ProductAdminService;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductAdminController.class)
@Import({SecurityConfig.class, JwtAccessDeniedHandler.class, JwtAuthenticationEntryPoint.class})
class ProductAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductAdminService productAdminService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RedisTokenService redisTokenService;

    @Test
    @DisplayName("관리자 상품 생성 성공 테스트")
    void createProductSuccess() throws Exception {
        // given
        ProductCreateRequestDto requestDto = ProductCreateRequestDto.builder()
                .name("Test Product")
                .model("Model-123")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(10000))
                .stock(100)
                .partnerId(1L)
                .build();

        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(1L)
                .name("Test Product")
                .model("Model-123")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(10000))
                .build();

        given(productAdminService.createProduct(any(ProductCreateRequestDto.class))).willReturn(responseDto);

        User adminUser = new User("admin", "password", Role.ADMIN);
        UserDetailsImpl adminUserDetails = UserDetailsImpl.build(adminUser);

        // when & then
        mockMvc.perform(post("/api/admin/products")
                        .with(csrf())
                        .with(user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value(ResponseText.CREATE_PRODUCT_SUCCESS.getMsg()))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("관리자 권한 없는 접근 실패 테스트")
    void createProductFailForbidden() throws Exception {
        // given
        ProductCreateRequestDto requestDto = ProductCreateRequestDto.builder()
                .name("Test Product")
                .model("Model-123")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(10000))
                .stock(100)
                .partnerId(1L)
                .build();

        User normalUser = new User("user", "password", Role.USER);
        UserDetailsImpl userDetails = UserDetailsImpl.build(normalUser);

        // when & then
        mockMvc.perform(post("/api/admin/products")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}