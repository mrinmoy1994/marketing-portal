package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class KYCApprovalRequest {
    @NonNull
    private String userId;
    private boolean panVerified;
    private boolean bankDetailsVerified;
    private boolean addressVerified;
}
