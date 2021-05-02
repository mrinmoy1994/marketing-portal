package in.mrinmoy.example.authentication.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "notification")
public class Notification {
    @Id
    private String id;
    private String type;
    private String userId;
    private String alertTitle;
    private boolean acknowledged;
    private String updateTime;
    private String ownerType;

    public Notification() {
        this.id = UUID.randomUUID().toString();
    }
}
