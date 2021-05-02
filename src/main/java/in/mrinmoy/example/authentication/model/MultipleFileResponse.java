package in.mrinmoy.example.authentication.model;

import java.util.List;

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
public class MultipleFileResponse {
    List<UploadFileResponse> uploadFileResponses;
    private List<String> failedFiles;
}
