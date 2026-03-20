package com.revature.generator.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordGeneratorServiceTest {

    private PasswordGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new PasswordGeneratorService();
    }

    @Test
    void generatePassword_ShouldHaveCorrectLength() {
        int length = 16;
        com.revature.generator.dtos.PasswordGenerateRequest req = new com.revature.generator.dtos.PasswordGenerateRequest();
        req.setLength(length);
        req.setUppercase(true);
        req.setLowercase(true);
        req.setNumbers(true);
        req.setSymbols(true);
        req.setCount(1);
        
        String password = service.generatePasswords(req).get(0);
        assertEquals(length, password.length());
    }

    @Test
    void generatePassword_ShouldIncludeUpperCase_WhenRequested() {
        com.revature.generator.dtos.PasswordGenerateRequest req = new com.revature.generator.dtos.PasswordGenerateRequest();
        req.setLength(100);
        req.setUppercase(true);
        req.setLowercase(false);
        req.setNumbers(false);
        req.setSymbols(false);
        req.setCount(1);
        
        String password = service.generatePasswords(req).get(0);
        assertTrue(password.matches(".*[A-Z].*"));
        assertFalse(password.matches(".*[a-z].*"));
        assertFalse(password.matches(".*[0-9].*"));
    }

    @Test
    void generatePassword_ShouldIncludeSymbols_WhenRequested() {
        com.revature.generator.dtos.PasswordGenerateRequest req = new com.revature.generator.dtos.PasswordGenerateRequest();
        req.setLength(100);
        req.setUppercase(false);
        req.setLowercase(false);
        req.setNumbers(false);
        req.setSymbols(true);
        req.setCount(1);
        
        String password = service.generatePasswords(req).get(0);
        assertTrue(password.chars().anyMatch(c -> "!@#$%^&*()_+{}[]<>?".indexOf(c) >= 0));
    }
}
