package be.kicksync_backend.feature.settlement.repository;

import be.kicksync_backend.feature.settlement.entity.Settlement;
import be.kicksync_backend.feature.settlement.entity.SettlementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, SettlementId> {
}
