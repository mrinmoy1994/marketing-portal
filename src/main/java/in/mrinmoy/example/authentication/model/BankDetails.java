package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// @Table(name = "bankDetails")
public class BankDetails {
    // @Id
    // @Column(name = "kyc_id")
    // private Long id;
    private String ifscCode;
    private String accountNo;

    // @OneToOne
    // @MapsId
    // @JoinColumn(name = "kyc_id")
    // private KYCDetails kycDetails;
}
