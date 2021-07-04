package in.mrinmoy.example.authentication.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class MultiImageUploadResponse {
    List<ImageResponse> successfulResources;
    List<String> failedResources;
}
