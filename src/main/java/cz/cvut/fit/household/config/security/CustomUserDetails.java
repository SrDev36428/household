package cz.cvut.fit.household.config.security;

import cz.cvut.fit.household.datamodel.entity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final transient User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Not implemented.
     *
     * @return True.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Not implemented.
     *
     * @return True.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Not implemented.
     *
     * @return True.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Not implemented.
     *
     * @return True.
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
