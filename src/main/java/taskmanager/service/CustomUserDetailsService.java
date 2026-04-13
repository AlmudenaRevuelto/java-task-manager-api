package taskmanager.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import taskmanager.model.User;
import taskmanager.repository.UserRepository;

/**
 * Implementation of {@link UserDetailsService} that loads users from the database.
 *
 * <p>Used by Spring Security during authentication to resolve a username to a
 * {@link UserDetails} object. The corresponding password must be BCrypt-encoded
 * in the database.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user by username and maps it to a {@link UserDetails} object.
     *
     * @param username the username to look up
     * @return the {@link UserDetails} for the found user
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}