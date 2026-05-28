package be.kicksync_backend.feature.settlement.batch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final SettlementSkipListener skipListener;

    private static final int CHUNK_SIZE = 1000;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRowDto {
        private Long id;
        private Long partnerId;
        private BigDecimal finalPrice;
    }

    @Bean("stepTaskExecutor")
    public TaskExecutor stepTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("settlement-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementManagerStep())
                .build();
    }

    @Bean
    public Step settlementManagerStep() {
        return new StepBuilder("settlementManagerStep", jobRepository)
                .partitioner("workerStep", partitioner())
                .step(workerStep())
                .gridSize(10)
                .taskExecutor(stepTaskExecutor())
                .build();
    }

    @Bean
    @JobScope
    public Partitioner partitioner() {
        return new PartnerIdPartitioner(dataSource);
    }

    @Bean
    public Step workerStep() {
        return new StepBuilder("workerStep", jobRepository)
                .<OrderRowDto, OrderRowDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader(null, null))
                .writer(writer())
                .faultTolerant()
                .retryLimit(3)
                .retry(org.springframework.dao.TransientDataAccessException.class)
                .retry(org.springframework.dao.DeadlockLoserDataAccessException.class)
                .skipLimit(100)
                .skip(IllegalArgumentException.class)
                .skip(NullPointerException.class)
                .listener(skipListener)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<OrderRowDto> reader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId) {
        
        log.info("[Reader] 파티션 범위 스캔 시작: minId={}, maxId={}", minId, maxId);

        String sql = "SELECT id, partner_id, final_price FROM orders " +
                     "WHERE status = 'PURCHASE_CONFIRMED' " +
                     "AND partner_id BETWEEN ? AND ? " +
                     "ORDER BY partner_id";

        return new JdbcCursorItemReaderBuilder<OrderRowDto>()
                .name("reader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new BeanPropertyRowMapper<>(OrderRowDto.class))
                .queryArguments(minId, maxId)
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<OrderRowDto> writer() {
        return new SettlementWriter(dataSource);
    }

    @RequiredArgsConstructor
    public static class SettlementWriter implements ItemWriter<OrderRowDto> {
        private final DataSource dataSource;
        private NamedParameterJdbcTemplate jdbcTemplate;

        @Override
        public void write(Chunk<? extends OrderRowDto> chunk) throws Exception {
            if (jdbcTemplate == null) {
                jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            }

            // 1. Chunk-level In-Memory Aggregation
            Map<Long, BigDecimal> aggregatedMap = new HashMap<>();
            for (OrderRowDto order : chunk.getItems()) {
                if (order.getFinalPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("음수 결제 가격은 정산 처리가 불가능합니다. 주문 ID: " + order.getId());
                }
                aggregatedMap.merge(order.getPartnerId(), order.getFinalPrice(), BigDecimal::add);
            }

            // 2. Prepare Batch Parameters
            List<MapSqlParameterSource> batchParams = new ArrayList<>();
            LocalDate currentDate = LocalDate.now();
            for (Map.Entry<Long, BigDecimal> entry : aggregatedMap.entrySet()) {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("partnerId", entry.getKey());
                params.addValue("totalAmount", entry.getValue());
                params.addValue("currentDate", currentDate);
                batchParams.add(params);
            }

            // 3. High-Performance Bulk Upsert
            String sql = "INSERT INTO settlements (partner_id, total_amount, status, settlement_date, created_at, updated_at) " +
                         "VALUES (:partnerId, :totalAmount, 'PENDING', :currentDate, NOW(), NOW()) " +
                         "ON DUPLICATE KEY UPDATE " +
                         "total_amount = total_amount + VALUES(total_amount), " +
                         "updated_at = NOW()";

            jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
        }
    }

    public static class PartnerIdPartitioner implements Partitioner {
        private final JdbcTemplate jdbcTemplate;

        public PartnerIdPartitioner(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Override
        public Map<String, ExecutionContext> partition(int gridSize) {
            String query = "SELECT MIN(partner_id), MAX(partner_id) FROM orders WHERE status = 'PURCHASE_CONFIRMED'";
            Map<String, ExecutionContext> result = new HashMap<>();
            try {
                Map<String, Object> minMax = jdbcTemplate.queryForMap(query);
                Long min = minMax.get("MIN(partner_id)") != null ? ((Number) minMax.get("MIN(partner_id)")).longValue() : 1L;
                Long max = minMax.get("MAX(partner_id)") != null ? ((Number) minMax.get("MAX(partner_id)")).longValue() : 10000L;

                long targetSize = (max - min) / gridSize + 1;
                long number = 0;
                long start = min;
                long end = start + targetSize - 1;

                while (start <= max) {
                    ExecutionContext value = new ExecutionContext();
                    result.put("partition" + number, value);
                    if (end >= max) {
                        end = max;
                    }
                    value.putLong("minId", start);
                    value.putLong("maxId", end);
                    start += targetSize;
                    end += targetSize;
                    number++;
                }
            } catch (Exception e) {
                log.error("[Partitioner] 파티션 분할 중 예외 발생, 기본 파티션 범위로 폴백합니다. 에러: {}", e.getMessage());
                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext value = new ExecutionContext();
                    value.putLong("minId", i * 1000 + 1);
                    value.putLong("maxId", (i + 1) * 1000);
                    result.put("partition" + i, value);
                }
            }
            return result;
        }
    }
}
