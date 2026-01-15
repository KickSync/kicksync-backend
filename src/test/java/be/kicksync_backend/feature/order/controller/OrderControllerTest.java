package be.kicksync_backend.feature.order.controller;

import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.order.dto.AddressDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderItemRequestDto;
import be.kicksync_backend.feature.order.repository.OrderItemRepository;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.token.RefreshTokenRepository;
import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.cache.type=none")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;
    private Product testProduct;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanUp();

        testUser = new User("testuser_order", passwordEncoder.encode("password123!"));
        userRepository.saveAndFlush(testUser);

        testProduct = Product.builder()
                .name("Test Product")
                .model("TP-123")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(100000))
                .stock(10)
                .partnerId(1L)
                .build();
        productRepository.save(testProduct);

        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(userDetails);

        accessToken = getAccessToken();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").clear();
        }
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getAccessToken() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(testUser.getUsername(), "password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        return JsonPath.parse(responseBody).read("$.data.accessToken");
    }

    @Test
    @DisplayName("주문 생성 API 성공 테스트")
    void createOrderSuccess() throws Exception {
        // given
        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .orderItems(List.of(new OrderItemRequestDto(testProduct.getId(), 2)))
                .receiverName("Receiver")
                .receiverPhone("010-1234-5678")
                .address(new AddressDto("12345", "Street", "Detail"))
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("주문이 성공적으로 생성되었습니다. 결제를 진행해주세요."))
                .andExpect(jsonPath("$.data.finalPrice").value(200000));
    }

    @Test
    @DisplayName("주문 목록 조회 API 성공 테스트")
    void getUserOrdersSuccess() throws Exception {
        // given: 먼저 주문 생성
        createOrderSuccess();

        // when & then
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("주문 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].finalPrice").value(200000));
    }
}
