package com.wedservice.backend.module.auth.security;

import com.wedservice.backend.module.user.entity.User;
import com.wedservice.backend.module.user.repository.UserRepository;
import com.wedservice.backend.module.user.util.UserContactNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // login bằng username/password hoặc khi bạn check JWT (filter của bạn cũng dùng)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedLogin = UserContactNormalizer.normalizeLoginIdentifier(username);

        User user = userRepository.findByLoginIdentifier(normalizedLogin)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));

        return CustomUserDetails.fromUser(user);
    }
}
