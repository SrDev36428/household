package cz.cvut.fit.household.config.security;

import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.repository.user.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(username)
                .orElseThrow(() -> {
                    log.error("Error occurred loading user - {} " , username);
                    return new UsernameNotFoundException("Wrong username or password");
                });

        return new CustomUserDetails(user);
    }
}
