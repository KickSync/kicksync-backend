package be.kicksync_backend.feature.order.controller;

import be.kicksync_backend.common.config.SecurityConfig;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.security.jwt.JwtAccessDeniedHandler;
import be.kicksync_backend.common.security.jwt.JwtAuthenticationEntryPoint;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.order.dto.AddressDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderItemRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.order.service.OrderFacade;
import be.kicksync_backend.feature.order.service.OrderService;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderFacade orderFacade;

    @MockitoBean
    private OrderService orderService;

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

    @BeforeEach
    void setUp() {
        testUser = new User("orderUser", "password");
        setUserId(testUser, 1L);

        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

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
    @DisplayName("주문 생성 성공 테스트")
    void createOrderSuccess() throws Exception {
        // given
        Long productId = 100L;
        OrderItemRequestDto itemDto = new OrderItemRequestDto(productId, 1);
        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .orderItems(List.of(itemDto))
                .receiverName("Receiver")
                .receiverPhone("010-1234-5678")
                .address(new AddressDto("12345", "Street", "Detail"))
                .build();

        OrderResponseDto responseDto = OrderResponseDto.builder()
                .orderId(1L)
                .finalPrice(BigDecimal.valueOf(10000))
                .merchantUid("merchant_123")
                .build();

        given(orderFacade.createOrderWithLock(any(), anyLong())).willReturn(Collections.singletonList(responseDto));

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("주문이 성공적으로 생성되었습니다. 결제를 진행해주세요."))
                .andExpect(jsonPath("$.data[0].orderId").value(1L))
                .andExpect(jsonPath("$.data[0].merchantUid").value("merchant_123"));
    }
}