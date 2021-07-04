package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Name {
    private String firstName;
    private String middleName;
    private String lastName;
}
