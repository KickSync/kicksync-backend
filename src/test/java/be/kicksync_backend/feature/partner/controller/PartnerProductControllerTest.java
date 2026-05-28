package be.kicksync_backend.feature.partner.controller;

import be.kicksync_backend.common.config.SecurityConfig;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.security.jwt.JwtAccessDeniedHandler;
import be.kicksync_backend.common.security.jwt.JwtAuthenticationEntryPoint;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.partner.service.PartnerProductService;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.entity.Role;
import be.kicksync_backend.feature.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartnerProductController.class)
@Import(SecurityConfig.class)
class PartnerProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerProductService partnerProductService;

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

    private User testUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        testUser = new User("partnerUser", "password", Role.PARTNER);
        setUserId(testUser, 1L);

        userDetails = UserDetailsImpl.build(testUser);
    }

    private void setUserId(User user, Long id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("상품 등록 성공 테스트")
    void createProductSuccess() throws Exception {
        // given
        ProductCreateRequestDto requestDto = ProductCreateRequestDto.builder()
                .name("New Kicks")
                .model("NK-2024-001")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(100000))
                .stock(100)
                .partnerId(1L)
                .build();

        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(1L)
                .name("New Kicks")
                .model("NK-2024-001")
                .retailPrice(BigDecimal.valueOf(100000))
                .build();

        given(partnerProductService.createProduct(any(ProductCreateRequestDto.class), any(Long.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/partner/products")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("상품 생성에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("내 상품 목록 조회 성공 테스트")
    void getMyProductsSuccess() throws Exception {
        // given
        ProductResponseDto productDto = ProductResponseDto.builder()
                .id(1L)
                .name("My Kicks")
                .build();
        Page<ProductResponseDto> page = new PageImpl<>(Collections.singletonList(productDto));

        given(partnerProductService.getMyProducts(any(Pageable.class), any(Long.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/partner/products")
                        .with(user(userDetails))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상품 전체 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @DisplayName("상품 수정 성공 테스트")
    void updateProductSuccess() throws Exception {
        // given
        ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto("Updated Name", "Updated Model", LocalDate.now(), BigDecimal.valueOf(120000), 100);
        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .build();

        given(partnerProductService.updateProduct(eq(1L), any(ProductUpdateRequestDto.class), any(Long.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/partner/products/{productId}", 1L)
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상품 수정에 성공하였습니다."));
    }

    @Test
    @DisplayName("상품 삭제 성공 테스트")
    void deleteProductSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/partner/products/{productId}", 1L)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상품 삭제에 성공하였습니다."));
    }
}
