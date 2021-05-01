package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String password;
    private String username;
    private String mailId;
    private String phone;
    private String type;
    private boolean verified;
    private String referralCode;
    private Name name;
    private Double balance;

    public User() {
        this.id = UUID.randomUUID().toString();
    }
}
