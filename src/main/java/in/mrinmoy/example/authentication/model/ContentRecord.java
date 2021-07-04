package in.mrinmoy.example.authentication.model;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nonapi.io.github.classgraph.json.Id;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "ContentRecord")
public class ContentRecord {
    @Id
    private String id;
    private String contentId;
    private String clientIp;
    private String userId;
    private String seenTime;

    public ContentRecord() {
        this.id = UUID.randomUUID().toString();
    }
}
