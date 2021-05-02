package in.mrinmoy.example.authentication.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
