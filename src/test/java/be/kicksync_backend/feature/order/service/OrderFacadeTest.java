package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.feature.order.dto.AddressDto;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderItemRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.repository.OrderItemRepository;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.payment.service.PaymentService;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false",
        "jwt.secret.key=testSecretKey1234567890testSecretKey1234567890",
        "jwt.access.expiration=3600000",
        "jwt.refresh.expiration=86400000",
        "iamport.api.key=testIamportKey",
        "iamport.api.secret=testIamportSecret",
        "toss.api.key=testTossKey"
})
class OrderFacadeTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private PaymentService paymentService;

    private User testUser;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = new User("facadeUser", passwordEncoder.encode("password"));
        userRepository.saveAndFlush(testUser);

        product1 = Product.builder()
                .name("Product 1")
                .model("M1")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(10000))
                .stock(100)
                .partnerId(1L)
                .build();
        productRepository.saveAndFlush(product1);

        product2 = Product.builder()
                .name("Product 2")
                .model("M2")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(20000))
                .stock(100)
                .partnerId(1L)
                .build();
        productRepository.saveAndFlush(product2);
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        try {
            paymentRepository.deleteAllInBatch();
            orderItemRepository.deleteAllInBatch();
            orderRepository.deleteAllInBatch();
            productRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("H2 MySQL 모드에서 DataIntegrityViolation 발생 - 테스트 환경 수정 필요")
    @Test
    @DisplayName("주문 취소 프로세스 검증: Start -> Refund -> Finalize")
    void cancelOrder_Flow_Success() throws Exception {
        // given
        OrderCreateRequestDto createDto = OrderCreateRequestDto.builder()
                .orderItems(List.of(
                        new OrderItemRequestDto(product1.getId(), 1)
                ))
                .receiverName("Test Receiver")
                .receiverPhone("010-0000-0000")
                .address(new AddressDto("12345", "Seoul", "Gangnam"))
                .build();

        OrderResponseDto orderResponse = orderFacade.createOrderWithLock(createDto, testUser.getId());
        Long orderId = orderResponse.getOrderId();

        // createOrder가 PENDING_PAYMENT로 설정하므로 PAYMENT_COMPLETED 상태를 수동으로 시뮬레이션
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.processPaymentSuccess(); // PENDING -> PREPARING
        orderRepository.save(order);

        OrderCancelRequestDto cancelDto = new OrderCancelRequestDto("Simple Change");

        // when
        orderFacade.cancelOrder(orderId, cancelDto, testUser.getId());

        // then
        // 1. PaymentService 호출 검증
        verify(paymentService).cancelPaymentForOrder(eq(orderId), eq("Simple Change"));

        // 2. 최종 상태 검증
        Order finalOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        
        // 3. 재고 복구 검증
        Product finalProduct = productRepository.findById(product1.getId()).orElseThrow();
        assertThat(finalProduct.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("Deadlock 방지 테스트: 서로 반대 순서로 주문 시에도 정상 처리되어야 함")
    void createOrder_Deadlock_Prevention() throws InterruptedException {
        // given
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        User userA = new User("userA", passwordEncoder.encode("password"));
        User userB = new User("userB", passwordEncoder.encode("password"));
        userRepository.saveAndFlush(userA);
        userRepository.saveAndFlush(userB);

        // User A: Item 1, Item 2
        OrderCreateRequestDto requestA = OrderCreateRequestDto.builder()
                .orderItems(List.of(
                        new OrderItemRequestDto(product1.getId(), 1),
                        new OrderItemRequestDto(product2.getId(), 1)
                ))
                .receiverName("User A")
                .receiverPhone("010-1111-1111")
                .address(new AddressDto("12345", "Seoul", "Gangnam"))
                .build();

        // User B: Item 2, Item 1
        OrderCreateRequestDto requestB = OrderCreateRequestDto.builder()
                .orderItems(List.of(
                        new OrderItemRequestDto(product2.getId(), 1),
                        new OrderItemRequestDto(product1.getId(), 1)
                ))
                .receiverName("User B")
                .receiverPhone("010-2222-2222")
                .address(new AddressDto("12345", "Seoul", "Gangnam"))
                .build();

        // when
        executorService.submit(() -> {
            try {
                orderFacade.createOrderWithLock(requestA, userA.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                orderFacade.createOrderWithLock(requestB, userB.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(2);
    }
}
