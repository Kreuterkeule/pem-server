package com.kreuterkeule.PEM.controller;

import com.kreuterkeule.PEM.dto.AuthResponseDto;
import com.kreuterkeule.PEM.dto.LoginDto;
import com.kreuterkeule.PEM.dto.RegisterDto;
import com.kreuterkeule.PEM.models.RoleEntity;
import com.kreuterkeule.PEM.models.UserEntity;
import com.kreuterkeule.PEM.repositories.RoleRepository;
import com.kreuterkeule.PEM.repositories.UserRepository;
import com.kreuterkeule.PEM.security.JwtUtils;
import com.kreuterkeule.PEM.services.UniqueTokenProviderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtUtils jwtUtils;

    private UniqueTokenProviderService uniqueTokenProviderService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UniqueTokenProviderService uniqueTokenProviderService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.uniqueTokenProviderService = uniqueTokenProviderService;
    }

    @PostMapping(path = "login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateToken(authentication, request);

        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }

    @PostMapping("logout")
    public ResponseEntity<AuthResponseDto> logout(HttpServletRequest request) {

        return new ResponseEntity<>(new AuthResponseDto(""), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username already taken!", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setIdentifierToken(uniqueTokenProviderService.generateToken()); // TODO: make unique token really unique

        RoleEntity roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);

        return new ResponseEntity<>("User registered", HttpStatus.OK);
    }

    @GetMapping("delete")
    public ResponseEntity<UserEntity> delete(@RequestParam("id") int id, HttpServletRequest request, Model model) {

        String token = request.getHeader("Authorization").substring(7);
        UserEntity user = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(token)).get();
        if (user.getId() == id) {
            user.setRoles(new ArrayList<>());
            userRepository.deleteById(id);

            return ResponseEntity.ok(user);
        }
        System.out.println("User '" + user.getUsername() + "' is trying to delete user with id '" + id + "' but isn't the user, so he isn't permitted");
        UserEntity badRequestUser = new UserEntity();
        badRequestUser.setUsername("BadRequest");
        badRequestUser.setPassword("BadRequest");
        return ResponseEntity.ok(badRequestUser);
    }

    @GetMapping("getUserInfo")
    public ResponseEntity<UserEntity> getUserInfo(HttpServletRequest request) {
        return new ResponseEntity<>(
                userRepository.findByUsername(
                        jwtUtils.getUsernameFromJwt(
                                request.getHeader(
                                        "Authorization"
                                ).substring(
                                        7
                                )
                        )
                ).get(),
                HttpStatus.OK);
    }

}
