package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "notification")
public class Notification {
    @Id
    private String id;
    private String type;
    private Long userId;
    private String alertTitle;
    private boolean acknowledged;

    public Notification() {
        this.id = UUID.randomUUID().toString();
    }
}
