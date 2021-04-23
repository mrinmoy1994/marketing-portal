package in.mrinmoy.example.authentication.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class LoggedInUser implements UserDetails {
    private final String id;
    private final String password;
    private final String username;
    private final String mailId;
    private final String phone;

    public LoggedInUser(User user) {
        this.id = user.getId();
        this.mailId = user.getMailId();
        this.phone = user.getPhone();
        this.password = user.getPassword();
        this.username = user.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
