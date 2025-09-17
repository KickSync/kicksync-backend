package be.kicksync_backend.common.dto;

import lombok.Getter;

@Getter
public enum ResponseText {

    // Auth
    ADMIN_SIGNUP_SUCCESS("관리자 '%s' 등록이 완료되었습니다."),
    ADMIN_LOGIN_SUCCESS("관리자 '%s' 로그인에 성공했습니다."),
    USER_SIGNUP_SUCCESS("사용자 '%s' 등록이 완료되었습니다."),
    USER_LOGIN_SUCCESS("사용자 '%s' 로그인에 성공했습니다."),
    TOKEN_REFRESH_SUCCESS("액세스 토큰이 성공적으로 갱신되었습니다.");

    private final String msg;

    ResponseText(String msg) {
        this.msg = msg;
    }

    public String format(Object... args) {
        return String.format(this.msg, args);
    }
}
