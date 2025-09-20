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

    // Product
    CREATE_PRODUCT_SUCCESS("상품 생성에 성공하였습니다."),
    GET_PRODUCTS_SUCCESS("상품 전체 조회에 성공하였습니다."),
    GET_PRODUCT_SUCCESS("상품 단건 조회에 성공하였습니다."),
    UPDATE_PRODUCT_SUCCESS("상품 수정에 성공하였습니다."),
    DELETE_PRODUCT_SUCCESS("상품 삭제에 성공하였습니다.");

    private final String msg;

    ResponseText(String msg) {
        this.msg = msg;
    }

    public String format(Object... args) {
        return String.format(this.msg, args);
    }
}
