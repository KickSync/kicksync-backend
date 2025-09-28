package be.kicksync_backend.feature.batch;

import be.kicksync_backend.feature.batch.listener.PerformanceStepExecutionListener;
import be.kicksync_backend.feature.batch.processor.SettlementItemProcessor;
import be.kicksync_backend.feature.payment.dto.PartnerSettlementDto;
import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final PerformanceStepExecutionListener performanceStepExecutionListener;

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               JpaPagingItemReader<PartnerSettlementDto> settlementItemReader,
                               ItemProcessor<PartnerSettlementDto, Settlement> settlementItemProcessor,
                               ItemWriter<Settlement> settlementItemWriter) {
        return new StepBuilder("settlementStep", jobRepository)
                .<PartnerSettlementDto, Settlement>chunk(1000, transactionManager)
                .reader(settlementItemReader)
                .processor(settlementItemProcessor)
                .writer(settlementItemWriter)
                .listener(performanceStepExecutionListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<PartnerSettlementDto> settlementItemReader(
            @Value("#{jobParameters['settlementDate']}") String settlementDateStr) {

        LocalDate settlementDate = LocalDate.parse(settlementDateStr);
        LocalDateTime startOfDay = settlementDate.atStartOfDay();
        LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", PaymentStatus.PAID);
        parameters.put("startDate", startOfDay);
        parameters.put("endDate", endOfDay);

        String queryString = String.format("SELECT NEW %s(p.partnerId, SUM(p.paymentAmount)) " +
                        "FROM Payment p " +
                        "WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate AND p.partnerId IS NOT NULL " +
                        "GROUP BY p.partnerId " +
                        "ORDER BY p.partnerId",
                PartnerSettlementDto.class.getName());

        return new JpaPagingItemReaderBuilder<PartnerSettlementDto>()
                .name("settlementItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(queryString)
                .parameterValues(parameters)
                .pageSize(1000) // 청크 사이즈와 동일하게 설정하여 메모리 최적화
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<PartnerSettlementDto, Settlement> settlementItemProcessor(
            @Value("#{jobParameters['settlementDate']}") String settlementDateStr) {
        return new SettlementItemProcessor(LocalDate.parse(settlementDateStr));
    }

    @Bean
    public ItemWriter<Settlement> settlementItemWriter() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
