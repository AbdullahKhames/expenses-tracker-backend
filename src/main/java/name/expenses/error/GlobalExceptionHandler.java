package name.expenses.error;

import lombok.extern.slf4j.Slf4j;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ResponseDto> handleNotFound(ObjectNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        ResponseError error = ResponseError.builder()
                .errorCategory(ErrorCategory.DATABASE_Error)
                .errorCode("DB_001")
                .errorMessage(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDtoBuilder.getErrorResponse(804, error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDtoBuilder.getErrorResponse(804, errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ResponseError error = ResponseError.builder()
                .errorCategory(ErrorCategory.BusinessError)
                .errorMessage("You are not authorized to access this resource")
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseDtoBuilder.getErrorResponse(810, error));
    }

    @ExceptionHandler(GeneralFailureException.class)
    public ResponseEntity<ResponseDto> handleGeneralFailure(GeneralFailureException ex) {
        log.error("General failure: {}", ex.getMessage());
        ResponseError error = ResponseError.builder()
                .errorCategory(ErrorCategory.BusinessError)
                .errorMessage(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDtoBuilder.getErrorResponse(804, error));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseDto> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failure: {}", ex.getMessage());
        ResponseError error = ResponseError.builder()
                .errorCategory(ErrorCategory.BusinessError)
                .errorMessage(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDtoBuilder.getErrorResponse(810, error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ResponseError error = ResponseError.builder()
                .errorCategory(ErrorCategory.BusinessError)
                .errorMessage("An unexpected error occurred. Please try again later.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDtoBuilder.getErrorResponse(804, error));
    }
}
