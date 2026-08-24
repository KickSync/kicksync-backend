package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.feature.order.dto.AddressDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderItemRequestDto;
import be.kicksync_backend.feature.order.repository.OrderItemRepository;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = "spring.batch.job.enabled=false")
public class OrderServiceConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PartnerRepository partnerRepository;

    private Product testProduct;
    private User testUser;
    private Partner testPartner;

    @BeforeEach
    void setUp() {
        testPartner = Partner.builder()
                .name("Test Partner")
                .businessNumber("123-45-67890")
                .commissionRate(BigDecimal.valueOf(0.05))
                .contactEmail("partner@test.com")
                .bankName("Bank")
                .accountNumber("1234-5678")
                .accountHolder("Holder")
                .build();
        partnerRepository.saveAndFlush(testPartner);

        testProduct = Product.builder()
                .name("한정판 신발")
                .model("KS-2024-LE")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(300000))
                .stock(1)
                .partner(testPartner)
                .build();
        productRepository.saveAndFlush(testProduct);

        testUser = new User("testuser", passwordEncoder.encode("password"));
        userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        partnerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("100개의 동시 주문 요청 시 재고 차감 동시성 테스트 (분산 락)")
    void concurrencyTest_with_DistributedLock() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        OrderItemRequestDto orderItemDto = OrderItemRequestDto.builder()
                .productId(testProduct.getId())
                .quantity(1)
                .build();

        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .orderItems(List.of(orderItemDto))
                .receiverName("테스트")
                .receiverPhone("010-1234-5678")
                .address(new AddressDto("12345", "서울시", "강남구"))
                .build();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderFacade.createOrderWithLock(requestDto, testUser.getId());
                    successCount.incrementAndGet();
                } catch (CustomException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Product finalProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(0, finalProduct.getStock(), "최종 재고는 0이어야 합니다.");
        assertEquals(1, successCount.get(), "성공한 주문은 1개여야 합니다.");
        assertEquals(threadCount - 1, failCount.get(), "실패한 주문은 99개여야 합니다.");
    }
}