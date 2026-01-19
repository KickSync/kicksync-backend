package be.kicksync_backend.feature.partner.repository;

import be.kicksync_backend.feature.partner.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByUser_Id(Long userId);
}
