package be.kicksync_backend.feature.batch;

import be.kicksync_backend.feature.batch.listener.PerformanceStepExecutionListener;
import be.kicksync_backend.feature.batch.listener.SettlementSkipListener;
import be.kicksync_backend.feature.batch.partitioner.PartnerIdRangePartitioner;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final PerformanceStepExecutionListener performanceStepExecutionListener;
    private final SettlementSkipListener settlementSkipListener;
    private final DataSource dataSource;

    @Bean("stepTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("settlement-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step workerStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(workerStep)
                .build();
    }

    @Bean
    public Step settlementManagerStep(JobRepository jobRepository,
                                      Step workerStep,
                                      Partitioner partitioner,
                                      @Qualifier("stepTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("settlementManagerStep", jobRepository)
                .partitioner("workerStep", partitioner)
                .step(workerStep)
                .gridSize(10)
                .taskExecutor(taskExecutor)
                .build();
    }


    @Bean
    @JobScope
    public Partitioner partitioner(
            @Value("#{jobParameters['settlementDate'] ?: null}") String settlementDateStr,
            @Value("#{jobParameters['startDate'] ?: null}") String startDateStr,
            @Value("#{jobParameters['endDate'] ?: null}") String endDateStr) throws JobParametersInvalidException {

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

        return new PartnerIdRangePartitioner(dataSource, startDate, endDate);
    }


    @Bean
    public Step workerStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           JpaPagingItemReader<be.kicksync_backend.feature.order.entity.Order> settlementItemReader,
                           ItemWriter<be.kicksync_backend.feature.order.entity.Order> settlementItemWriter) {
        return new StepBuilder("workerStep", jobRepository)
                .<be.kicksync_backend.feature.order.entity.Order, be.kicksync_backend.feature.order.entity.Order>chunk(1000, transactionManager)
                .reader(settlementItemReader)
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
    public JpaPagingItemReader<be.kicksync_backend.feature.order.entity.Order> settlementItemReader(
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
         parameters.put("purchaseConfirmedStatus", OrderStatus.PURCHASE_CONFIRMED);
         parameters.put("startDate", startDate);
         parameters.put("endDate", endDate);

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("SELECT o FROM Order o ");
        queryStringBuilder.append("WHERE o.status = :purchaseConfirmedStatus ");
        queryStringBuilder.append("AND o.orderDate BETWEEN :startDate AND :endDate ");

        if (partnerIdsStr != null && !partnerIdsStr.isEmpty()) {
            List<Long> partnerIds = Arrays.stream(partnerIdsStr.split(","))
                    .map(Long::parseLong)
                    .toList();
            queryStringBuilder.append("AND o.partnerId IN :partnerIds ");
            parameters.put("partnerIds", partnerIds);
        }

        queryStringBuilder.append("ORDER BY o.id");

        return new JpaPagingItemReaderBuilder<be.kicksync_backend.feature.order.entity.Order>()
                .name("settlementItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(queryStringBuilder.toString())
                .parameterValues(parameters)
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemWriter<be.kicksync_backend.feature.order.entity.Order> settlementItemWriter() {
        return new ItemWriter<be.kicksync_backend.feature.order.entity.Order>() {
            private org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbcTemplate;

            @Override
            public void write(org.springframework.batch.item.Chunk<? extends be.kicksync_backend.feature.order.entity.Order> chunk) throws Exception {
                if (jdbcTemplate == null) {
                    jdbcTemplate = new org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate(dataSource);
                }

                String sql = "INSERT INTO settlements (partner_id, total_amount, status, settlement_date, created_at, updated_at) " +
                             "VALUES (:partnerId, :totalAmount, 'PENDING', :currentDate, NOW(), NOW()) " +
                             "ON DUPLICATE KEY UPDATE " +
                             "total_amount = total_amount + VALUES(total_amount), " +
                             "updated_at = NOW()";

                LocalDate currentDate = LocalDate.now();
                for (be.kicksync_backend.feature.order.entity.Order order : chunk.getItems()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("partnerId", order.getPartnerId());
                    params.put("totalAmount", order.getFinalPrice());
                    params.put("currentDate", currentDate);

                    // 건건이 동기식으로 update를 호출하여 네트워크 왕복 지연 및 Gap Lock 경합 유도
                    jdbcTemplate.update(sql, params);
                }
            }
        };
    }
}