package be.kicksync_backend.feature.user.controller;

import be.kicksync_backend.common.entity.Address;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.token.RefreshTokenRepository;
import be.kicksync_backend.feature.user.dto.ProfileUpdateRequestDto;
import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "spring.cache.type=none")
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        partnerRepository.deleteAll();

        String uniqueUsername = "mypage_user_" + System.nanoTime();
        testUser = new User(uniqueUsername, passwordEncoder.encode("password123!"));
        userRepository.saveAndFlush(testUser);

       Partner partner = Partner.builder()
                .name("Test Partner")
                .businessNumber("123-45-67890")
                .commissionRate(BigDecimal.ZERO)
                .build();
        partnerRepository.saveAndFlush(partner);

        Product testProduct = Product.builder()
                .name("Test Product")
                .model("MP-123_" + System.nanoTime())
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(100000))
                .stock(10)
                .partner(partner)
                .build();
        productRepository.saveAndFlush(testProduct);

        Address address = new Address("12345", "Street", "Detail");
        OrderItem orderItem = OrderItem.builder()
                .product(testProduct)
                .quantity(1)
                .orderPrice(testProduct.getRetailPrice())
                .build();

        Order testOrder = Order.builder()
                .user(testUser)
                .address(address)
                .receiverName("Receiver")
                .receiverPhone("010-1234-5678")
                .requestMessage("Message")
                .orderItems(java.util.List.of(orderItem))
                .partnerId(testProduct.getPartner().getId())
                .merchantUid("merchant_" + System.nanoTime())
                .build();
        orderRepository.saveAndFlush(testOrder);
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
    @DisplayName("내 프로필 조회 성공 테스트")
    void getMyProfileSuccess() throws Exception {
        // given
        String accessToken = getAccessToken();

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/mypage/profile")
                .header("Authorization", "Bearer " + accessToken));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("프로필 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.username").value(testUser.getUsername()));
    }

    @Test
    @DisplayName("내 프로필 수정 성공 테스트")
    void updateMyProfileSuccess() throws Exception {
        // given
        String accessToken = getAccessToken();
        ProfileUpdateRequestDto requestDto = new ProfileUpdateRequestDto("nickname");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(patch("/api/mypage/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("프로필 수정에 성공하였습니다."));

        User updatedUser = userRepository.findByUsername(testUser.getUsername()).orElseThrow();
        assertEquals("nickname", updatedUser.getNickname());
    }
}