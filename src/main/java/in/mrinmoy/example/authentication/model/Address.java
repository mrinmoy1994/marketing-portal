package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
//@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    // @Id
    // @Column(name = "kyc_id")
    // private Long id;
    private String baseAddress;
    private String city;
    private String state;
    private String country;
    private String pin;

    // @OneToOne
    // @MapsId
    // @JoinColumn(name = "kyc_id")
    // private KYCDetails kycDetails;
}
