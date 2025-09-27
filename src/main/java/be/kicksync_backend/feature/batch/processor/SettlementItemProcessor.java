package be.kicksync_backend.feature.batch.processor;

import be.kicksync_backend.feature.payment.dto.PartnerSettlementDto;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import be.kicksync_backend.feature.settlement.entity.SettlementStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;

@Slf4j
public class SettlementItemProcessor implements ItemProcessor<PartnerSettlementDto, Settlement> {

    @Override
    public Settlement process(PartnerSettlementDto partnerSettlementDto) throws Exception {
        if (partnerSettlementDto == null || partnerSettlementDto.getPartnerId() == null) {
            log.warn("PartnerSettlementDto가 null이거나 partnerId가 없습니다.");
            return null;
        }

        log.debug("파트너 {} 정산 처리 중: 총액 {}", 
                partnerSettlementDto.getPartnerId(), 
                partnerSettlementDto.getTotalAmount());

        return Settlement.builder()
                .partnerId(partnerSettlementDto.getPartnerId())
                .totalAmount(partnerSettlementDto.getTotalAmount())
                .settlementDate(LocalDate.now().minusDays(4))
                .status(SettlementStatus.COMPLETED)
                .build();
    }
}
