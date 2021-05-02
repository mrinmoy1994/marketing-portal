package in.mrinmoy.example.authentication.model;

import java.util.UUID;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "photos")
public class Image {
    @Id
    private String id;
    private Binary content;
    private String name;
    private String extension;
    private String type;
    private Long updatedTime;
    private String userId;

    public Image() {
        this.id = UUID.randomUUID().toString();
    }
}
