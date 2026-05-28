package be.kicksync_backend.feature.batch;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import be.kicksync_backend.feature.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
public class SettlementJobRalphLoopTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job settlementJob;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private be.kicksync_backend.feature.user.repository.UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(settlementJob);
        settlementRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSettlementJobRalphLoop() throws Exception {
        // [Given] 유저 및 주소 설정
        be.kicksync_backend.feature.user.entity.User user = new be.kicksync_backend.feature.user.entity.User("testuser", "password");
        userRepository.save(user);

        be.kicksync_backend.common.entity.Address address = new be.kicksync_backend.common.entity.Address("street", "detail", "12345");

        // 1. 구매 확정 주문 (정산 대상이어야 함)
        Order confirmedOrder = Order.builder()
                .user(user)
                .partnerId(1L)
                .merchantUid("REF-CONFIRMED")
                .address(address)
                .receiverName("Tester")
                .receiverPhone("010-0000-0000")
                .orderItems(List.of())
                .build();
        
        // 상태 전이: PENDING_PAYMENT -> PREPARING -> SHIPPED -> DELIVERED -> PURCHASE_CONFIRMED
        confirmedOrder.processPaymentSuccess();
        confirmedOrder.ship();
        confirmedOrder.deliver();
        confirmedOrder.confirmPurchase();
        
        // Reflection을 통해 finalPrice 설정 (테스트용)
        setFinalPrice(confirmedOrder, BigDecimal.valueOf(100000));
        orderRepository.save(confirmedOrder);

        // 2. 배송 완료 주문 (정산 대상이 아니어야 함)
        Order deliveredOrder = Order.builder()
                .user(user)
                .partnerId(2L)
                .merchantUid("REF-DELIVERED")
                .address(address)
                .receiverName("Tester")
                .receiverPhone("010-0000-0000")
                .orderItems(List.of())
                .build();
        
        // 상태 전이: PENDING_PAYMENT -> PREPARING -> SHIPPED -> DELIVERED
        deliveredOrder.processPaymentSuccess();
        deliveredOrder.ship();
        deliveredOrder.deliver();
        
        setFinalPrice(deliveredOrder, BigDecimal.valueOf(200000));
        orderRepository.save(deliveredOrder);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("settlementDate", LocalDate.now().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // [When] Job 실행
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // [Then] 정산 데이터가 1건이어야 함 (PURCHASE_CONFIRMED 상태만 정산 대상)
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        
        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).hasSize(1);
        
        Settlement settlement = settlements.get(0);
        assertThat(settlement.getPartnerId()).isEqualTo(1L);
        assertThat(settlement.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
    }

    private void setFinalPrice(Order order, BigDecimal price) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("finalPrice");
            field.setAccessible(true);
            field.set(order, price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
