package be.kicksync_backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Auth
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "가입되지 않은 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 관리자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 공백만으로 지정할 수 없습니다."),
    UNAUTHORIZED_SUBSCRIBE(HttpStatus.FORBIDDEN, "구독 권한을 확인할 수 없습니다."),

    // Drop Event
    DROP_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 드롭 이벤트를 찾을 수 없습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
    PRODUCT_IN_USE(HttpStatus.CONFLICT, "해당 상품은 주문 또는 드롭 이벤트에서 사용 중이므로 삭제할 수 없습니다."),
    PRODUCT_UPDATE_CONFLICT(HttpStatus.CONFLICT, "다른 관리자에 의해 정보가 수정되었습니다. 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}