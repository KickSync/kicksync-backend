package be.kicksync_backend.feature.payment.controller;

import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.service.PaymentService;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import be.kicksync_backend.feature.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        testUser = new User("paymentUser", passwordEncoder.encode("password"));
        setUserId(testUser, 1L);

        given(userRepository.findByUsername("paymentUser")).willReturn(Optional.of(testUser));

        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
        accessToken = jwtUtil.generateAccessToken(userDetails);

        given(userService.loadUserByUsername("paymentUser")).willReturn(userDetails);
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
    @DisplayName("내 결제 내역 조회 API 성공 테스트")
    void getMyPaymentsSuccess() throws Exception {
        // given
        Payment payment = Payment.builder()
                .paymentAmount(BigDecimal.valueOf(10000))
                .paymentMethod("card")
                .pgProvider("html5_inicis")
                .pgType("payment")
                .impUid("imp_1234567890")
                .merchantUid("merchant_1234567890")
                .pgTid("pg_1234567890")
                .status(be.kicksync_backend.feature.payment.entity.PaymentStatus.PAID)
                .paymentDate(LocalDateTime.now())
                .user(testUser)
                .order(mock(Order.class)) 
                .build();
        
        given(paymentService.getMyPayments(testUser.getId())).willReturn(Collections.singletonList(payment));

        // when & then
        mockMvc.perform(get("/api/payments")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("결제 내역 목록을 찾았습니다."));
    }
}