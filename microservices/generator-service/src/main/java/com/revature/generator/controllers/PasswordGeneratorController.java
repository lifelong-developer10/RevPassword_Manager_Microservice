package com.revature.generator.controllers;

import com.revature.generator.dtos.PasswordGenerateRequest;
import com.revature.generator.dtos.PasswordGenerateResponse;
import com.revature.generator.services.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
public class PasswordGeneratorController {

    private final PasswordGeneratorService generatorService;

    /** GET /api/generator/generate?length=16 — quick generation with defaults */
    @GetMapping("/generate")
    public PasswordGenerateResponse generate(
            @RequestParam(value = "length", defaultValue = "12") int length) {

        PasswordGenerateRequest request = new PasswordGenerateRequest();
        request.setLength(length);
        request.setCount(1);
        request.setUppercase(true);
        request.setLowercase(true);
        request.setNumbers(true);
        request.setSymbols(true);

        List<String> passwords = generatorService.generatePasswords(request);
        return new PasswordGenerateResponse(passwords);
    }

    /** POST /api/generator/generate — full options from Angular generator page */
    @PostMapping("/generate")
    public PasswordGenerateResponse generateWithOptions(
            @RequestBody PasswordGenerateRequest request) {

        // Guard: ensure at least one character set is selected
        if (!request.isUppercase() && !request.isLowercase()
                && !request.isNumbers() && !request.isSymbols()) {
            request.setLowercase(true);
        }
        if (request.getLength() < 4) request.setLength(4);
        if (request.getCount() < 1) request.setCount(1);
        if (request.getCount() > 20) request.setCount(20);

        List<String> passwords = generatorService.generatePasswords(request);
        return new PasswordGenerateResponse(passwords);
    }
}
