package in.mrinmoy.example.authentication.model;

import java.util.UUID;

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
@Document(collection = "kycDetails")
public class KYCDetails {
    @Id
    private String id;
    private String panImageId;
    private String aadharImageId;
    private String bankImageId;
    private String userId;
    private Address address;
    private BankDetails bankDetails;
    private String panNo;
    private String aadharNo;
    private boolean panApproved;
    private boolean bankDetailsApproved;
    private boolean aadharApproved;

    public KYCDetails() {
        this.id = UUID.randomUUID().toString();
    }
}
