package be.kicksync_backend.common.dto;

import lombok.Getter;

@Getter
public enum ResponseText {

    // Auth
    ADMIN_SIGNUP_SUCCESS("관리자 등록이 완료되었습니다."),
    ADMIN_LOGIN_SUCCESS("관리자 로그인에 성공했습니다."),
    USER_SIGNUP_SUCCESS("회원가입에 성공하였습니다."),
    USER_LOGIN_SUCCESS("로그인에 성공하였습니다."),
    LOGOUT_SUCCESS("로그아웃에 성공하였습니다."),
    DELETE_ACCOUNT_SUCCESS("회원탈퇴에 성공하였습니다."),
    TOKEN_REFRESH_SUCCESS("토큰 재발급에 성공하였습니다.");

    private final String msg;

    /**
     * Creates an enum constant with the associated response message.
     *
     * @param msg the message string for this response (may contain `String.format` placeholders)
     */
    ResponseText(String msg) {
        this.msg = msg;
    }

    /**
     * Formats this enum's message with the provided arguments.
     *
     * Returns the enum's stored message after applying String.format with the given arguments,
     * allowing insertion of values into any format specifiers present in the message.
     *
     * @param args values referenced by the format specifiers in the message
     * @return the formatted message string
     */
    public String format(Object... args) {
        return String.format(this.msg, args);
    }
}
