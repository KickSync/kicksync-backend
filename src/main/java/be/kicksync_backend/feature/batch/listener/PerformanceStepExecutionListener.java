package be.kicksync_backend.feature.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PerformanceStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        long startTime = System.currentTimeMillis();
        stepExecution.getExecutionContext().putLong("startTime", startTime);

        long startMemory = getUsedMemory();
        stepExecution.getExecutionContext().putLong("startMemory", startMemory);

        log.info("Step '{}' 시작. 시작 메모리: {} MB",
                stepExecution.getStepName(),
                startMemory);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long startTime = stepExecution.getExecutionContext().getLong("startTime");
        long duration = System.currentTimeMillis() - startTime;

        long startMemory = stepExecution.getExecutionContext().getLong("startMemory");
        long endMemory = getUsedMemory();
        long usedMemory = endMemory - startMemory;

        log.info("-------------------------------------------------------");
        log.info("Step '{}' 종료. ExitStatus: {}", stepExecution.getStepName(), stepExecution.getExitStatus().getExitCode());
        log.info("읽은 건수: {}, 처리한 건수: {}, 저장한 건수: {}",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getWriteCount());
        log.info("총 실행 시간: {} ms", duration);
        log.info("메모리 사용량 변화: 시작 {} MB -> 종료 {} MB (사용량: {} MB)",
                startMemory,
                endMemory,
                usedMemory);
        log.info("-------------------------------------------------------");

        return stepExecution.getExitStatus();
    }

    /**
     * 현재 사용 중인 힙 메모리 양을 MB 단위로 반환합니다.
     */
    private long getUsedMemory() {

        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
} 