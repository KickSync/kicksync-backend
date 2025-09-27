package be.kicksync_backend.feature.batch;

import be.kicksync_backend.feature.batch.processor.SettlementItemProcessor;
import be.kicksync_backend.feature.payment.dto.PartnerSettlementDto;
import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final PaymentRepository paymentRepository;

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemReader<PartnerSettlementDto> settlementItemReader,
                               ItemProcessor<PartnerSettlementDto, Settlement> settlementItemProcessor,
                               ItemWriter<Settlement> settlementItemWriter) {
        return new StepBuilder("settlementStep", jobRepository)
                .<PartnerSettlementDto, Settlement>chunk(100, transactionManager)
                .reader(settlementItemReader)
                .processor(settlementItemProcessor)
                .writer(settlementItemWriter)
                .build();
    }

    @Bean
    public ItemReader<PartnerSettlementDto> settlementItemReader() {
        return new ItemReader<PartnerSettlementDto>() {
            private List<PartnerSettlementDto> partnerSettlementData;
            private int currentIndex = 0;

            @Override
            public PartnerSettlementDto read() {
                if (partnerSettlementData == null) {
                    LocalDate settlementDate = LocalDate.now().minusDays(4);
                    LocalDateTime startOfDay = settlementDate.atStartOfDay();
                    LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);
                    
                    log.info("정산 대상 날짜: {} ~ {}", startOfDay, endOfDay);
                    
                    partnerSettlementData = paymentRepository.findPartnerTotalsByStatusAndPaymentDateBetween(
                            PaymentStatus.PAID, startOfDay, endOfDay);
                    
                    log.info("집계된 파트너 정산 데이터: {}건", partnerSettlementData.size());
                    currentIndex = 0;
                }

                if (currentIndex < partnerSettlementData.size()) {
                    return partnerSettlementData.get(currentIndex++);
                }
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<PartnerSettlementDto, Settlement> settlementItemProcessor() {
        return new SettlementItemProcessor();
    }

    @Bean
    public ItemWriter<Settlement> settlementItemWriter() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
