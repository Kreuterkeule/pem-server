package com.kreuterkeule.PEM.services;

import com.kreuterkeule.PEM.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UniqueTokenProviderService {

    private final UserRepository userRepository;

    @Autowired
    public UniqueTokenProviderService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 23;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        System.out.println(generatedString);

        for (int i = 0; i < 3; i++) {
            generatedString = generatedString.substring(0, (i + 1) * 6) + '-' + generatedString.substring(((i+1) * 6) + 1);
        }
        String firstChar = generatedString.substring(0, 1);
        generatedString = generatedString.substring(1) + firstChar;

        if(userRepository.findByIdentifierToken(generatedString).isEmpty()) {
            return generatedString;
        }
        return this.generateToken();

    }

}
