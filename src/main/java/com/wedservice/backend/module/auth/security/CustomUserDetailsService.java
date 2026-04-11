package com.wedservice.backend.module.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.common.util.DataNormalizer;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // login bằng username/password hoặc khi bạn check JWT (filter của bạn cũng dùng)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedLogin = DataNormalizer.normalizeLoginIdentifier(username);

        User user = userRepository.findByLoginIdentifier(normalizedLogin)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));

        return CustomUserDetails.fromUser(user);
    }
}
