package com.revature.generator.services;

import com.revature.generator.dtos.PasswordGenerateRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordGeneratorService {

    private static final String UPPER =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String LOWER =
            "abcdefghijklmnopqrstuvwxyz";

    private static final String NUM =
            "0123456789";

    private static final String SYM =
            "!@#$%^&*()_+{}[]<>?";

    private static final String SIMILAR =
            "0O1lI";

    private final SecureRandom random = new SecureRandom();

    public List<String> generatePasswords(
            PasswordGenerateRequest req) {

        StringBuilder pool = new StringBuilder();

        if (req.isUppercase()) pool.append(UPPER);
        if (req.isLowercase()) pool.append(LOWER);
        if (req.isNumbers()) pool.append(NUM);
        if (req.isSymbols()) pool.append(SYM);

        // Default to lowercase if nothing was selected
        if (pool.length() == 0) pool.append(LOWER);

        String chars = pool.toString();

        if (req.isExcludeSimilar()) {
            for (char c : SIMILAR.toCharArray()) {
                chars = chars.replace(String.valueOf(c), "");
            }
        }

        if (chars.isEmpty()) {
            // Fallback: use lowercase letters if the pool is somehow empty
            chars = LOWER;
        }

        List<String> list = new ArrayList<>();

        for (int i = 0; i < req.getCount(); i++) {
            list.add(generate(chars, req.getLength()));
        }

        return list;
    }

    private String generate(String chars, int length) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(
                    random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
