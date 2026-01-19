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
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요."),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 관리자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 공백만으로 지정할 수 없습니다."),
    UNAUTHORIZED_SUBSCRIBE(HttpStatus.FORBIDDEN, "구독 권한을 확인할 수 없습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Drop Event
    DROP_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 드롭 이벤트를 찾을 수 없습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
    PARTNER_NOT_FOUND(HttpStatus.NOT_FOUND, "입점사를 찾을 수 없습니다."),
    PRODUCT_IN_USE(HttpStatus.CONFLICT, "다른 주문 또는 이벤트에서 사용 중인 상품은 삭제할 수 없습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "주문 처리 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요."),

    // 5xx
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),
    PRODUCT_UPDATE_CONFLICT(HttpStatus.CONFLICT, "다른 관리자에 의해 정보가 수정되었습니다. 다시 시도해주세요."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),

    // Payment
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_MERCHANT_UID_MISMATCH(HttpStatus.BAD_REQUEST, "주문번호가 일치하지 않습니다."),
    PAYMENT_STATUS_NOT_PAID(HttpStatus.BAD_REQUEST, "결제가 완료되지 않은 주문입니다."),
    PAYMENT_VERIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 정보 조회에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND_AFTER_DUPLICATION(HttpStatus.INTERNAL_SERVER_ERROR, "중복 저장 후 결제 정보를 찾을 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
    MULTIPLE_PARTNERS_IN_ORDER(HttpStatus.BAD_REQUEST, "한 번의 주문에는 동일한 파트너의 상품만 담을 수 있습니다."),
    ORDER_CANCEL_NOT_ALLOWED_SHIPPED(HttpStatus.BAD_REQUEST, "배송이 시작된 상품은 취소가 불가능합니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    ORDER_SHIP_NOT_ALLOWED_NOT_PREPARING(HttpStatus.BAD_REQUEST, "배송 준비 중인 상품만 배송을 시작할 수 있습니다."),
    ORDER_DELIVER_NOT_ALLOWED_NOT_SHIPPED(HttpStatus.BAD_REQUEST, "배송 중인 상품만 배송 완료 처리할 수 있습니다."),
    ORDER_SETTLE_NOT_ALLOWED_NOT_DELIVERED(HttpStatus.BAD_REQUEST, "배송 완료된 상품만 정산 처리할 수 있습니다."),
    ORDER_CREATE_SUCCESS(HttpStatus.CREATED, "주문이 성공적으로 생성되었습니다. 결제를 진행해주세요."),
    ORDER_DETAIL_FETCH_SUCCESS(HttpStatus.OK, "주문 상세 조회 성공"),
    ORDER_LIST_FETCH_SUCCESS(HttpStatus.OK, "주문 목록 조회 성공"),
    ORDER_CANCEL_SUCCESS(HttpStatus.OK, "주문이 성공적으로 취소되었습니다."),
    ORDER_CANCEL_FAILED_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "주문 취소 처리 중 오류가 발생했습니다."),

    // Payment
    INVALID_ORDER_STATE(HttpStatus.BAD_REQUEST, "현재 주문 상태에서는 해당 작업을 수행할 수 없습니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 결제입니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "결제 취소에 실패했습니다."),

    // S3
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일입니다."),

    // Common
    DATABASE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 업데이트에 실패했습니다, 관리자에게 문의하세요."),
    EMPTY_ORDER_ITEMS(HttpStatus.MULTI_STATUS, "주문 항목이 비어 있습니다.");
    private final HttpStatus status;
    private final String message;
}