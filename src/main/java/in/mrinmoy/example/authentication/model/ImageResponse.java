package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponse {
    private String id;
    private String name;
    private String extension;
    private String type;
    private String subType;
    private String updatedTime;
    private String userId;

    public ImageResponse(Image image) {
        this.id = image.getId();
        this.name = image.getName();
        this.extension = image.getExtension();
        this.type = image.getType();
        this.subType = image.getSubType();
        this.updatedTime = image.getUpdatedTime();
        this.userId = image.getUserId();
    }
}
