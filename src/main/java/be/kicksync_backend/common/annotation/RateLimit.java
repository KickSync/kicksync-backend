package be.kicksync_backend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis INCR 원자 연산 기반의 Lock-free Rate Limiter를 위한 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 레이트 리미트 대상이 되는 키 식별자 (예: products)
     */
    String key() default "default";

    /**
     * 허용 요청 횟수 (제한량)
     */
    int limit() default 100;

    /**
     * 제한 시간(초 단위윈도우)
     */
    long window() default 60L;
}
