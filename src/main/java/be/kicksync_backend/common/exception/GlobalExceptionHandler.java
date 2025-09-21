package be.kicksync_backend.common.exception;

import be.kicksync_backend.common.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        ErrorCode errorCode = ErrorCode.INVALID_PASSWORD;
        ErrorResponse response = new ErrorResponse(errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException e) {
        ErrorCode errorCode = ErrorCode.PRODUCT_UPDATE_CONFLICT;
        ErrorResponse response = new ErrorResponse(errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}