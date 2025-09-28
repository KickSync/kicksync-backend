package be.kicksync_backend.feature.batch;

import be.kicksync_backend.feature.batch.listener.PerformanceStepExecutionListener;
import be.kicksync_backend.feature.batch.listener.SettlementSkipListener;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.JobParametersInvalidException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final PerformanceStepExecutionListener performanceStepExecutionListener;
    private final SettlementSkipListener settlementSkipListener;

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
                .faultTolerant()
                .retryLimit(3)
                .retry(OptimisticLockingFailureException.class)
                .retry(PessimisticLockingFailureException.class)
                .skipLimit(100)
                .skip(NullPointerException.class)
                .skip(IllegalArgumentException.class)
                .listener(settlementSkipListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<PartnerSettlementDto> settlementItemReader(
            @Value("#{jobParameters['settlementDate'] ?: null}") String settlementDateStr,
            @Value("#{jobParameters['startDate'] ?: null}") String startDateStr,
            @Value("#{jobParameters['endDate'] ?: null}") String endDateStr,
            @Value("#{jobParameters['partnerIds'] ?: null}") String partnerIdsStr) throws JobParametersInvalidException {

        LocalDateTime startDate;
        LocalDateTime endDate;

        if (startDateStr != null && endDateStr != null) {
            startDate = LocalDate.parse(startDateStr).atStartOfDay();
            endDate = LocalDate.parse(endDateStr).atTime(LocalTime.MAX);
        } else if (settlementDateStr != null) {
            LocalDate settlementDate = LocalDate.parse(settlementDateStr);
            startDate = settlementDate.atStartOfDay();
            endDate = settlementDate.atTime(LocalTime.MAX);
        } else {
            throw new JobParametersInvalidException("settlementDate 또는 startDate, endDate 파라미터가 필요합니다.");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", PaymentStatus.PAID);
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(String.format("SELECT NEW %s(p.partnerId, SUM(p.paymentAmount)) ", PartnerSettlementDto.class.getName()));
        queryStringBuilder.append("FROM Payment p ");
        queryStringBuilder.append("WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate AND p.partnerId IS NOT NULL ");

        if (partnerIdsStr != null && !partnerIdsStr.isEmpty()) {
            List<Long> partnerIds = Arrays.stream(partnerIdsStr.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            queryStringBuilder.append("AND p.partnerId IN :partnerIds ");
            parameters.put("partnerIds", partnerIds);
        }

        queryStringBuilder.append("GROUP BY p.partnerId ");
        queryStringBuilder.append("ORDER BY p.partnerId");


        return new JpaPagingItemReaderBuilder<PartnerSettlementDto>()
                .name("settlementItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(queryStringBuilder.toString())
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
