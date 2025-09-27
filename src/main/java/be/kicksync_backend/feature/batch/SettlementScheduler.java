package be.kicksync_backend.feature.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    @Scheduled(cron = "0 * * * * ?") // 매분 실행 (테스트용)
    @SchedulerLock(name = "dailySettlementTask")
    public void runSettlementJob() throws Exception {
        long startTime = System.currentTimeMillis();
        LocalDate settlementDate = LocalDate.now().minusDays(4);

        log.info("=== 일일 정산 배치 작업 시작 ===");
        log.info("정산 대상 날짜: {}", settlementDate);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("settlementDate", settlementDate.toString())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(settlementJob, jobParameters);

            long duration = System.currentTimeMillis() - startTime;

            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                log.info("=== 일일 정산 배치 작업 완료 ===");
                log.info("실행 상태: {}", jobExecution.getStatus());
                log.info("실행 시간: {}ms", duration);

                // Step별 실행 결과 로그
                for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                    log.info("Step: {}, 읽은 건수: {}, 처리한 건수: {}, 저장한 건수: {}",
                            stepExecution.getStepName(),
                            stepExecution.getReadCount(),
                            stepExecution.getProcessSkipCount(),
                            stepExecution.getWriteCount());
                }
            } else {
                log.error("=== 일일 정산 배치 작업 실패 ===");
                log.error("실행 상태: {}", jobExecution.getStatus());
                log.error("실행 시간: {}ms", duration);

                // 에러 로그
                for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                    if (!stepExecution.getFailureExceptions().isEmpty()) {
                        log.error("Step {} 실패 원인: {}",
                                stepExecution.getStepName(),
                                stepExecution.getFailureExceptions());
                    }
                }
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== 일일 정산 배치 작업 예외 발생 ===");
            log.error("실행 시간: {}ms", duration);
            log.error("예외 메시지: {}", e.getMessage(), e);
            throw e;
        }
    }
}
