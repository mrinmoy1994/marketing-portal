package in.mrinmoy.example.authentication.util;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionUtil {

    public static CustomException getException(String message, String remediation, HttpStatus httpStatus) {
        CustomException ex = new CustomException(message);
        ex.setHttpStatus(httpStatus);
        ex.setRemediation(remediation);
        return ex;
    }

    public static ResponseEntity<?> getExceptionResponse(CustomException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .code(e.getHttpStatus().value())
                .error(e.getMessage())
                .remediation(e.getRemediation())
                .build(), e.getHttpStatus());
    }
}
