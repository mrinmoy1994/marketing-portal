package in.mrinmoy.example.authentication.model;

import org.springframework.security.core.GrantedAuthority;

public class authority implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "admin";
    }
}
