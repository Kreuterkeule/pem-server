package com.kreuterkeule.PEM.service;

import com.kreuterkeule.PEM.configuration.CustomUserDetails;
import com.kreuterkeule.PEM.model.UserEntity;
import com.kreuterkeule.PEM.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userRepo.getUserByUsername(username).orElseThrow();
        return new CustomUserDetails(user);
    }
}
