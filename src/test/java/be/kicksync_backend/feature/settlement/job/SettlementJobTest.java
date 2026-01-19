package be.kicksync_backend.feature.settlement.job;

import be.kicksync_backend.feature.batch.SettlementJobConfig;
import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import be.kicksync_backend.feature.settlement.repository.SettlementRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.order.entity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {be.kicksync_backend.KicksyncBackendApplication.class, SettlementJobConfig.class})
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false",
        "jwt.secret.key=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0cy13aGljaC1tdXN0LWJlLWxvbmctZW5vdWdo",
        "iamport.api.key=test",
        "iamport.api.secret=test",
        "toss.api.key=test"
})
class SettlementJobTest {

    @TestConfiguration
    static class BatchTestConfig {
        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PartnerRepository partnerRepository;

    @BeforeEach
    void setUp() {
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        partnerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("정산 배치 Job 실행 성공 테스트")
    void settlementJobSuccess() throws Exception {
        // given
        User user = new User("settlementUser", passwordEncoder.encode("password"));
        userRepository.saveAndFlush(user);

        Partner partner1 = Partner.builder().name("P1").businessNumber("1").commissionRate(BigDecimal.ZERO).build();
        partnerRepository.save(partner1);
        
        Partner partner2 = Partner.builder().name("P2").businessNumber("2").commissionRate(BigDecimal.ZERO).build();
        partnerRepository.save(partner2);

        // Partner 1: Total 30000
        createPayment(user, partner1, 10000, PaymentStatus.PAID);
        createPayment(user, partner1, 20000, PaymentStatus.PAID);

        // Partner 2: Total 5000 (10000 Paid - 5000 Cancelled)
        createPayment(user, partner2, 10000, PaymentStatus.PAID);
        createPayment(user, partner2, 5000, PaymentStatus.CANCELLED);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("settlementDate", LocalDate.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).hasSize(2);

        Settlement s1 = settlements.stream().filter(s -> s.getPartnerId().equals(partner1.getId())).findFirst().orElseThrow();
        assertThat(s1.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));

        Settlement s2 = settlements.stream().filter(s -> s.getPartnerId().equals(partner2.getId())).findFirst().orElseThrow();
        assertThat(s2.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    private void createPayment(User user, Partner partner, int amount, PaymentStatus status) {
        String merchantUid = "merchant_" + System.nanoTime();
        
        Product product = Product.builder()
                .name("Product")
                .model("Model_" + System.nanoTime())
                .releaseDate(LocalDate.now())
                .retailPrice(BigDecimal.valueOf(amount))
                .stock(10)
                .partner(partner)
                .build();
        productRepository.save(product);

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(1)
                .orderPrice(BigDecimal.valueOf(amount))
                .build();
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        Order order = Order.builder()
                .user(user)
                .address(new Address("12345", "Street", "Detail"))
                .receiverName("Receiver")
                .receiverPhone("010-1234-5678")
                .requestMessage("Msg")
                .orderItems(orderItems)
                .partnerId(partner.getId())
                .merchantUid(merchantUid)
                .build();
        orderRepository.save(order);
        
        if (status == PaymentStatus.PAID) {
            try {
                order.processPaymentSuccess();
            } catch (Exception e) {}
        } else if (status == PaymentStatus.CANCELLED) {
             try {
                order.processPaymentSuccess();
                order.cancel();
             } catch (Exception e) {}
        }
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .paymentAmount(BigDecimal.valueOf(amount))
                .paymentMethod("CARD")
                .pgProvider("PG")
                .pgType("TYPE")
                .impUid("imp_" + System.nanoTime())
                .merchantUid(merchantUid)
                .pgTid("pg_" + System.nanoTime())
                .status(status == PaymentStatus.CANCELLED ? PaymentStatus.PAID : status)
                .paymentDate(LocalDateTime.now())
                .user(user)
                .build();
        
        if (status == PaymentStatus.CANCELLED) {
            payment.updateOnCancel("Test Cancel");
        }
        
        paymentRepository.save(payment);
    }
}
