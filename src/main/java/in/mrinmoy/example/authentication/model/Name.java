package in.mrinmoy.example.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Name {
    //    @Id
//    @Column(name = "user_id")
//    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;

//    @OneToOne
//    @MapsId
//    @JoinColumn(name = "user_id")
//    private User user;

    public Name() {
        // this.id = UUID.randomUUID().toString();
    }
}
