package be.kicksync_backend.common.exception;

import be.kicksync_backend.common.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles CustomException by converting it to an ErrorResponse and returning an appropriate HTTP response.
     *
     * <p>Extracts the ErrorCode from the exception, creates an ErrorResponse using the code's message,
     * and returns a ResponseEntity with that body and the HTTP status from the ErrorCode.</p>
     *
     * @param e the CustomException containing the ErrorCode to map to an ErrorResponse
     * @return a ResponseEntity whose body is an ErrorResponse (built from the ErrorCode message)
     *         and whose HTTP status is taken from the ErrorCode
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * Handles Spring Security's BadCredentialsException by mapping it to the application's
     * INVALID_PASSWORD error code and returning a structured ErrorResponse with the
     * corresponding HTTP status.
     *
     * @return a ResponseEntity containing an ErrorResponse with the INVALID_PASSWORD message
     *         and the HTTP status defined by that ErrorCode
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        ErrorCode errorCode = ErrorCode.INVALID_PASSWORD;
        ErrorResponse response = new ErrorResponse(errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }
} 