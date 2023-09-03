package com.kreuterkeule.PEM.controller;
import com.kreuterkeule.PEM.security.JwtUtils;
import com.kreuterkeule.PEM.services.UniqueTokenProviderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin

public class HomeController {

    private final JwtUtils jwtUtils;

    private final UniqueTokenProviderService uniqueTokenProviderService;

    @Autowired
    public HomeController(JwtUtils jwtUtils, UniqueTokenProviderService uniqueTokenProviderService) {
        this.jwtUtils = jwtUtils;
        this.uniqueTokenProviderService = uniqueTokenProviderService;
    }

    @GetMapping("/getToken")
    public String getToken() {
        return uniqueTokenProviderService.generateToken();
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {

        String token =  request.getHeader("Authorization").substring(7);

        model.addAttribute("username", jwtUtils.getUsernameFromJwt(token));

        return "home";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {

        String token = jwtUtils.getJWTFromRequest(request);

        if (token == null) {
            System.out.println("non authenticated User viewed Login");
            return "login";
        }

        try {

            String username = jwtUtils.getUsernameFromJwt(token);

            System.out.println("user '" + username + "' viewed the Login Page, redirecting to logout.");

            return "redirect:/api/auth/logout";

        } catch (Exception e) {

            System.out.println("non authenticated User with Bearer token viewed Login");

            return "login";


        }

    }

}
