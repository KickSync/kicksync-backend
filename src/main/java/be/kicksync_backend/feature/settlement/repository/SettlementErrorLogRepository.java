package be.kicksync_backend.feature.settlement.repository;

import be.kicksync_backend.feature.settlement.entity.SettlementErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementErrorLogRepository extends JpaRepository<SettlementErrorLog, Long> {
}
