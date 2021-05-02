package in.mrinmoy.example.authentication.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomException extends Exception {
    private HttpStatus httpStatus;
    private String remediation;

    public CustomException(String message) {
        super(message);
    }
}
