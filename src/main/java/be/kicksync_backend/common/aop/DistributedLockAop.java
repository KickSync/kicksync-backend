package be.kicksync_backend.common.aop;

import be.kicksync_backend.common.annotation.DistributedLock;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.util.AopForTransaction;
import be.kicksync_backend.common.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @DistributedLock 어노테이션을 처리하는 AOP.
 * 락 획득 -> 트랜잭션 시작 -> 비즈니스 로직 -> 트랜잭션 커밋 -> 락 해제 순서를 보장함.
 * 단일 키 및 다중 키 지원.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(be.kicksync_backend.common.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        Object userId = getUserId(signature, joinPoint.getArgs());

        // SpEL을 사용하여 락 키 생성 (단일 객체 또는 컬렉션 반환 가능)
        Object dynamicValue = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

        RLock lock;
        String lockNameLog;

        // 다중 락 또는 단일 락 처리
        if (dynamicValue instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }
            
            List<RLock> locks = new ArrayList<>();
            List<String> lockNames = new ArrayList<>();

            // Deadlock 방지를 위해 Key 정렬
            List<String> sortedKeys = collection.stream()
                    .map(Object::toString)
                    .sorted()
                    .toList();

            for (String item : sortedKeys) {
                String lockKey = REDISSON_LOCK_PREFIX + "product:" + item;
                locks.add(redissonClient.getLock(lockKey));
                lockNames.add(lockKey);
            }

            lock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
            lockNameLog = "MultiLock(" + lockNames + ")";
        } else {
            String lockKey = REDISSON_LOCK_PREFIX + "product:" + dynamicValue.toString();
            lock = redissonClient.getLock(lockKey);
            lockNameLog = lockKey;
        }

        boolean isLocked = false;
        try {
            boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                log.warn("[Redisson Lock] 락 획득 실패: {} (User={})", lockNameLog, userId);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            isLocked = true;
            log.info("[Redisson Lock] 락 획득 성공: {} (User={})", lockNameLog, userId);
            return aopForTransaction.proceed(joinPoint);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (isLocked) {
                try {
                    lock.unlock();
                    log.info("[Redisson Lock] 락 해제 완료: {} (User={})", lockNameLog, userId);
                } catch (Exception e) {
                    log.warn("[Redisson Lock] 락 해제 중 예외 발생: {} (User={})", lockNameLog, userId, e);
                }
            }
        }
    }

    private Object getUserId(MethodSignature signature, Object[] args) {
        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals("userId")) {
                return args[i];
            }
        }
        return "Unknown";
    }
}
