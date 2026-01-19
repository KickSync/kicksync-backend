package be.kicksync_backend.feature.partner.controller;

import be.kicksync_backend.common.config.SecurityConfig;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.partner.service.PartnerProductService;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import be.kicksync_backend.feature.user.entity.Role;
import be.kicksync_backend.feature.user.entity.User;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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

    private User partnerUser;

    @BeforeEach
    void setUp() {
        partnerUser = new User("partnerUser", "password", Role.PARTNER);
        setUserId(partnerUser, 1L);

        UserDetailsImpl userDetails = UserDetailsImpl.build(partnerUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
    void createProduct_Success() throws Exception {
        // given
        ProductCreateRequestDto requestDto = ProductCreateRequestDto.builder()
                .name("New Kicks")
                .model("NK-001")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(150000))
                .stock(100)
                .partnerId(1L)
                .build();

        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(100L)
                .name("New Kicks")
                .model("NK-001")
                .retailPrice(BigDecimal.valueOf(150000))
                .build();

        given(partnerProductService.createProduct(any(ProductCreateRequestDto.class), any(User.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/partner/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("상품 생성에 성공하였습니다."))
                .andExpect(jsonPath("$.data.name").value("New Kicks"));
    }

    @Test
    @DisplayName("내 상품 목록 조회 성공 테스트")
    void getMyProducts_Success() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("createdAt"));
        ProductResponseDto productDto = ProductResponseDto.builder()
                .id(100L)
                .name("My Product")
                .build();
        Page<ProductResponseDto> page = new PageImpl<>(Collections.singletonList(productDto));

        given(partnerProductService.getMyProducts(any(), any(User.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/partner/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상품 전체 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.content[0].name").value("My Product"));
    }

    @Test
    @DisplayName("상품 수정 성공 테스트")
    void updateProduct_Success() throws Exception {
        // given
        Long productId = 100L;
        ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                "Updated Name", "Updated Model", LocalDate.now(), BigDecimal.valueOf(200000)
        );

        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(productId)
                .name("Updated Name")
                .retailPrice(BigDecimal.valueOf(200000))
                .build();

        given(partnerProductService.updateProduct(eq(productId), any(ProductUpdateRequestDto.class), any(User.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/partner/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상품 수정에 성공하였습니다."))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("상품 삭제 성공 테스트")
    void deleteProduct_Success() throws Exception {
        // given
        Long productId = 100L;

        // when & then
        mockMvc.perform(delete("/api/partner/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상품 삭제에 성공하였습니다."));

        verify(partnerProductService).deleteProduct(eq(productId), any(User.class));
    }
}
