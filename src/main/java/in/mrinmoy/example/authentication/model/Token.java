package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Token {
    @Id
    String id;
    String token;
    String userId;

    public Token() {
        this.id = UUID.randomUUID().toString();
    }
}
