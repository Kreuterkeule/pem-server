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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final UniqueTokenProviderService uniqueTokenProviderService;

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

    private void saveNewAdmin(String username, String password) throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        RoleEntity role = roleRepository.findByName("ADMIN").orElse(null);
        if (role == null) {
            throw new Exception("Please initialize the role table in the database with 0, ADMIN | 1, USER");
        }
        user.setRoles(Collections.singletonList(role));
        user.setIdentifierToken(uniqueTokenProviderService.generateToken());
        userRepository.save(user);
    }

    @PostMapping("registerAdmin")
    public ResponseEntity<String> registerAdmin(@RequestBody RegisterDto registerDto, HttpServletRequest request) throws Exception {
        if (userRepository.findByUsername(registerDto.getUsername()).orElse(null) != null) {
            return new ResponseEntity<>("Username taken", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findAll().isEmpty()) {
            this.saveNewAdmin(registerDto.getUsername(), registerDto.getPassword());
            return new ResponseEntity<>("First admin created successfully!!!", HttpStatus.OK);
        }
        if (userRepository.findAll().stream().allMatch(e -> e.getRoles().stream().noneMatch(f -> f.getName().equals("ADMIN")))) {
            this.saveNewAdmin(registerDto.getUsername(), registerDto.getPassword());
            return new ResponseEntity<>("First admin created successfully!!!", HttpStatus.OK);
        }
        final String header = request.getHeader("Authorization");
        if (header == null || header.equals("")) {
            return new ResponseEntity<>("Your authorization header is empty", HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = header.substring(7);
        UserEntity client = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(jwtToken)).orElse(null);
        if (client == null) {
            return new ResponseEntity<>("You are not logged in", HttpStatus.UNAUTHORIZED);
        }
        if (client.getRoles().stream().anyMatch(e -> e.getName().equals("ADMIN"))) {
            this.saveNewAdmin(registerDto.getUsername(), registerDto.getPassword());
            return new ResponseEntity<>("an admin created successfully!!!", HttpStatus.OK);
        }
        return new ResponseEntity<>("You are not permitted to create an ADMIN", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto, HttpServletRequest request) throws Exception {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username already taken!", HttpStatus.BAD_REQUEST);
        }
        if (request.getHeader("Authorization") == null ){
            return new ResponseEntity<>("You are not Authorized", HttpStatus.UNAUTHORIZED);
        }
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader.equals("")) {
            return new ResponseEntity<>("You are not Authorized, your header is empty", HttpStatus.UNAUTHORIZED);
        }
        String token = authorizationHeader.substring(7);
        UserEntity client = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(token)).orElse(null);
        if (client == null) {
            return new ResponseEntity<>("You are not logged in correctly!", HttpStatus.FORBIDDEN);
        }
        if (!client.getRoles().contains(roleRepository.findByName("ADMIN").orElse(null))) {
            return new ResponseEntity<>("You are not an Admin", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setIdentifierToken(uniqueTokenProviderService.generateToken()); // TODO: make unique token really unique

        RoleEntity roles = roleRepository.findByName("USER").orElse(null);
        if (roles == null) {
            throw new Exception("Please initialize the Database with: 0, ADMIN | 1, USER");
        }
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);

        return new ResponseEntity<>("User registered", HttpStatus.OK);
    }

    @GetMapping("delete")
    public ResponseEntity<UserEntity> delete(@RequestParam("id") int id, HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        UserEntity user = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(token)).orElse(null);
        if (user == null) {
            UserEntity $ = new UserEntity();
            $.setRoles(new ArrayList<>());
            $.setProjects(new ArrayList<>());
            return new ResponseEntity<>(new UserEntity(), HttpStatus.UNAUTHORIZED);
        }
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
                ).orElse(null),
                HttpStatus.OK);
    }
    @GetMapping("getUsers")
    public ResponseEntity<List<UserEntity>> getUsers() {
        return new ResponseEntity<>( userRepository.findAll().stream().peek(e -> {e.setPassword("****") /* hide password hashes from other users */;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }
}
