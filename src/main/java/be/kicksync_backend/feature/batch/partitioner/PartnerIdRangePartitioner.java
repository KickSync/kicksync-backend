package be.kicksync_backend.feature.batch.partitioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PartnerIdRangePartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public PartnerIdRangePartitioner(DataSource dataSource, LocalDateTime startDate, LocalDateTime endDate) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        String query = "SELECT MIN(partner_id), MAX(partner_id) FROM orders WHERE order_date BETWEEN ? AND ?";

        Map<String, ExecutionContext> result = new HashMap<>();

        try {
            jdbcTemplate.queryForObject(query, (rs, rowNum) -> {
                long min = rs.getLong(1);
                long max = rs.getLong(2);

                if (rs.wasNull()) {
                    log.warn("주어진 날짜 범위에 데이터가 없습니다: {} ~ {}", startDate, endDate);
                    return null;
                }

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
                    log.info("파티션 {} 생성됨: minId={}, maxId={}", number, start, end);

                    start += targetSize;
                    end += targetSize;
                    number++;
                }
                return null;
            }, Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));

        } catch (Exception e) {
            log.error("파티션 생성 중 오류 발생", e);
        }

        if (result.isEmpty()) {
            log.warn("파티션이 생성되지 않았습니다. Job Step이 데이터를 처리하지 않을 수 있습니다.");
        } else {
            log.info("총 {}개의 파티션이 생성되었습니다.", result.size());
        }
        return result;
    }
} 