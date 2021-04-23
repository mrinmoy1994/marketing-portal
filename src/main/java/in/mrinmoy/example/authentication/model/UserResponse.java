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
public class UserResponse {
    private String id;
    private String username;
    private String mailId;
    private String phone;
    private String type;
    private boolean verified;
    private String referralCode;
    private Name name;

    public UserResponse(User user) {
        this.id = String.valueOf(user.getId());
        this.username = user.getUsername();
        this.mailId = user.getMailId();
        this.phone = user.getPhone();
        this.type = user.getType();
        this.verified = user.isVerified();
        this.referralCode = user.getReferralCode();
        this.name = user.getName();
    }
}
