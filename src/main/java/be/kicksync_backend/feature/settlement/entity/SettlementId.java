package be.kicksync_backend.feature.settlement.entity;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SettlementId implements Serializable {
    private Long partnerId;
    private LocalDate settlementDate;
}
