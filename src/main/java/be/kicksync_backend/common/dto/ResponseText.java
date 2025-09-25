package be.kicksync_backend.common.dto;

import lombok.Getter;

@Getter
public enum ResponseText {

    // Auth
    ADMIN_SIGNUP_SUCCESS("관리자 등록이 완료되었습니다."),
    ADMIN_LOGIN_SUCCESS("관리자 로그인에 성공하였습니다."),
    USER_SIGNUP_SUCCESS("회원가입에 성공하였습니다."),
    USER_LOGIN_SUCCESS("로그인에 성공하였습니다."),
    LOGOUT_SUCCESS("로그아웃에 성공하였습니다."),
    DELETE_ACCOUNT_SUCCESS("회원탈퇴에 성공하였습니다."),
    TOKEN_REFRESH_SUCCESS("토큰 재발급에 성공하였습니다."),

    // MyPage
    PROFILE_GET_SUCCESS("프로필 조회에 성공하였습니다."),
    PROFILE_UPDATE_SUCCESS("프로필 수정에 성공하였습니다."),
    ORDER_HISTORY_GET_SUCCESS("주문 내역 조회에 성공하였습니다."),

    // Order
    ORDER_CREATE_SUCCESS("주문이 성공적으로 생성되었습니다. 결제를 진행해주세요."),
    ORDER_DETAIL_FETCH_SUCCESS("주문 상세 조회 성공"),
    ORDER_LIST_FETCH_SUCCESS("주문 목록 조회 성공"),
    ORDER_CANCEL_SUCCESS("주문이 성공적으로 취소되었습니다."),
    ORDER_CANCEL_FAIL("주문 취소 처리 중 오류가 발생했습니다."),

    // Product
    CREATE_PRODUCT_SUCCESS("상품 생성에 성공하였습니다."),
    GET_PRODUCTS_SUCCESS("상품 전체 조회에 성공하였습니다."),
    GET_PRODUCT_SUCCESS("상품 단건 조회에 성공하였습니다."),
    UPDATE_PRODUCT_SUCCESS("상품 수정에 성공하였습니다."),
    DELETE_PRODUCT_SUCCESS("상품 삭제에 성공하였습니다."),

    // Payment
    PAYMENT_VERIFICATION_SUCCESS("결제 검증이 완료되었습니다."),
    PAYMENT_CANCEL_SUCCESS("결제가 취소되었습니다."),
    PAYMENT_FOUND_SUCCESS("결제 내역을 찾았습니다."),
    PAYMENT_HISTORY_FOUND_SUCCESS("결제 내역 목록을 찾았습니다.");

    private final String msg;

    ResponseText(String msg) {
        this.msg = msg;
    }

    public String format(Object... args) {
        return String.format(this.msg, args);
    }
}
