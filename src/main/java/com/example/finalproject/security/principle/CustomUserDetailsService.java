package com.example.finalproject.security.principle;

import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new UsernameNotFoundException("User is not active");
        }

        // Support both authority checking (e.g. hasAuthority("ADMIN")) and role checking (e.g. hasRole("ADMIN"))
        String roleName = user.getRole();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority("ROLE_" + roleName);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(authority, roleAuthority))
                .build();
    }
}
