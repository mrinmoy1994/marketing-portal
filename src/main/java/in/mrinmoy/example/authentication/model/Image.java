package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "photos")
public class Image {
    @Id
    private String id;
    private Binary image;
    private String name;
    private String extension;
    private String type;
    private Long updatedTime;
    private String userId;

    public Image() {
        this.id = UUID.randomUUID().toString();
    }
}
