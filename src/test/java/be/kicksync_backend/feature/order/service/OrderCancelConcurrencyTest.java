package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.order.repository.OrderItemRepository;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import be.kicksync_backend.common.entity.Address;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "spring.datasource.hikari.maximum-pool-size=40"
})
public class OrderCancelConcurrencyTest {

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

    @Autowired
    private PaymentRepository paymentRepository;

    private Product testProduct;
    private User testUser;
    private Partner testPartner;
    private List<Long> orderIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        testPartner = Partner.builder()
                .name("Test Partner")
                .businessNumber("123-45-67890")
                .commissionRate(new BigDecimal("0.05"))
                .contactEmail("partner@test.com")
                .bankName("Bank")
                .accountNumber("1234-5678")
                .accountHolder("Holder")
                .build();
        partnerRepository.saveAndFlush(testPartner);

        testUser = new User("testuser_cancel", passwordEncoder.encode("password"));
        userRepository.saveAndFlush(testUser);

        testProduct = Product.builder()
                .name("Sold Out Kicks")
                .model("KS-2024-SOLDOUT")
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(100000))
                .stock(0)
                .partner(testPartner)
                .build();
        productRepository.saveAndFlush(testProduct);

        int orderCount = 20;
        for (int i = 0; i < orderCount; i++) {
            OrderItem item = OrderItem.builder()
                    .product(testProduct)
                    .quantity(1)
                    .orderPrice(BigDecimal.valueOf(100000))
                    .build();

            Order order = Order.builder()
                    .user(testUser)
                    .receiverName("Receiver " + i)
                    .receiverPhone("010-0000-0000")
                    .address(new Address("12345", "Seoul", "Gangnam"))
                    .orderItems(List.of(item))
                    .partnerId(testPartner.getId())
                    .merchantUid("merchant_" + i)
                    .build();

            orderRepository.save(order);
            orderIds.add(order.getId());
        }
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        partnerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("100개의 주문 동시 취소 시 재고 복구 동시성 테스트 (분산 락)")
    void cancelConcurrencyTest() throws InterruptedException {
        // given
        int threadCount = orderIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        OrderCancelRequestDto cancelDto = new OrderCancelRequestDto("Test Cancel");

        // when
        for (Long orderId : orderIds) {
            executorService.submit(() -> {
                try {
                    orderFacade.cancelOrder(orderId, cancelDto, testUser.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Product finalProduct = productRepository.findById(testProduct.getId()).orElseThrow();

        assertEquals(20, finalProduct.getStock(), 
            String.format("모든 주문 취소 후 재고는 20이어야 합니다. (Success: %d, Fail: %d)", successCount.get(), failCount.get()));
        assertEquals(20, successCount.get(), "모든 주문 취소가 성공해야 합니다.");
        assertEquals(0, failCount.get(), "실패한 주문 취소가 없어야 합니다.");
    }
}