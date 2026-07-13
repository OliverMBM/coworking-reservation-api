package com.coworking.reservation.security;

import com.coworking.reservation.entity.UserAccount;
import com.coworking.reservation.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserAccountRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con email: " + username
                        )
                );

        return User.builder()
                .username(userAccount.getEmail())
                .password(userAccount.getPassword())
                .authorities("ROLE_" + userAccount.getRole().name())
                .build();

    }
}
