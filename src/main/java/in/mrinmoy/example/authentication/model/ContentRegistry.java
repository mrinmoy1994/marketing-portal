package in.mrinmoy.example.authentication.model;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "ContentRegistry")
public class ContentRegistry {
    public String id;
    public String contentId;
    public String contentName;
    public String userId;
    public String downloadTime;

    public ContentRegistry() {
        this.id = UUID.randomUUID().toString();
    }
}
