package com.revature.vault.services;

import com.revature.vault.client.NotificationClient;
import com.revature.vault.dtos.PasswordEntryRequest;
import com.revature.vault.dtos.PasswordEntryResponse;
import com.revature.vault.models.AllPasswordEntry;
import com.revature.vault.security.EncryptionUtil;
import com.revature.vault.repository.PasswordEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasswordEntryService {

    private final PasswordEntryRepository repo;
    private final EncryptionUtil encryptionUtil;
    private final EncryptionService encryptionService;
    private final NotificationClient notificationClient;



    @Transactional
    public PasswordEntryResponse addEntry(
            String username,
            PasswordEntryRequest request) throws Exception {



        AllPasswordEntry entry = new AllPasswordEntry();

        entry.setAccountName(request.getAccountName());
        entry.setWebsite(request.getWebsite());
        entry.setUsername(request.getUsername());

        entry.setPasswordEncrypted(
                encryptionUtil.encrypt(request.getPassword()));

        entry.setCategory(request.getCategory());
        entry.setNotes(request.getNotes());
        entry.setFavorite(request.isFavorite());

        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        entry.setOwnerUsername(username);

        repo.save(entry);

        // NORMAL NOTIFICATION
        Map<String, String> notification = new HashMap<>();
        notification.put("username", username);
        notification.put("message", "Password saved for " + request.getAccountName());
        notification.put("type", "INFO");

        notificationClient.sendNotification(notification);


        // ⭐ WEAK PASSWORD CHECK
        String password = request.getPassword();

        if (isWeakPassword(password)) {

            Map<String, String> weakAlert = new HashMap<>();
            weakAlert.put("username", username);
            weakAlert.put(
                    "message",
                    "Security Alert: Weak password detected for " + request.getAccountName()
            );
            weakAlert.put("type", "SECURITY_ALERT");

            notificationClient.sendNotification(weakAlert);
        }



        return mapToResponse(entry);
    }

    private boolean isWeakPassword(String password) {

        if (password == null) return true;

        // weak if length < 8
        if (password.length() < 8) return true;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");

        return !(hasUpper && hasLower && hasDigit && hasSpecial);
    }

    public PasswordEntryResponse getLastEntry(String username) throws Exception {

        AllPasswordEntry entry =
                repo.findTopByOwnerUsernameOrderByCreatedAtDesc(username)
                        .orElse(null);

        if (entry == null) return null;

        PasswordEntryResponse res = new PasswordEntryResponse();

        res.setId(entry.getId());
        res.setAccountName(entry.getAccountName());
        res.setWebsite(entry.getWebsite());
        res.setUsername(entry.getUsername());

        res.setPassword(
                encryptionUtil.decrypt(entry.getPasswordEncrypted())
        );

        res.setCategory(entry.getCategory());
        res.setNotes(entry.getNotes());
        res.setFavorite(entry.isFavorite());

        return res;
    }

    public List<PasswordEntryResponse> getAllEntries(String username)
            throws Exception {

        return repo.findByOwnerUsername(username)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PasswordEntryResponse getEntry(Long id)
            throws Exception {

        AllPasswordEntry entry = repo.findById(id).orElseThrow();

        return mapToResponse(entry);
    }

    @Transactional
    public PasswordEntryResponse updateEntry(
            String ownerUsername,
            Long id,
            PasswordEntryRequest request) throws Exception {

        System.out.println("Updating entry ID: " + id + " for user: " + ownerUsername);

        AllPasswordEntry entry = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found with id: " + id));

        // Security check: ensure the requester owns this entry
        if (!entry.getOwnerUsername().equals(ownerUsername)) {
            throw new RuntimeException("Unauthorized: You do not own this entry.");
        }

        entry.setAccountName(request.getAccountName());
        entry.setWebsite(request.getWebsite());
        entry.setUsername(request.getUsername());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            entry.setPasswordEncrypted(encryptionUtil.encrypt(request.getPassword()));
        }

        entry.setCategory(request.getCategory());
        entry.setNotes(request.getNotes());
        entry.setFavorite(request.isFavorite());
        entry.setUpdatedAt(LocalDateTime.now());

        repo.save(entry);

        System.out.println("Entry updated successfully");

        return mapToResponse(entry);
    }

    @Transactional
    public void deleteEntry(Long id) {
        repo.deleteById(id);
    }

    public List<PasswordEntryResponse> getFavorites(String username)
            throws Exception {

        return repo.findByOwnerUsernameAndFavoriteTrue(username)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PasswordEntryResponse> search(
            String username, String keyword) throws Exception {

        return repo
                .findByOwnerUsernameAndAccountNameContainingIgnoreCase(
                        username, keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PasswordEntryResponse> filterByCategory(
            String username, String category) throws Exception {

        return repo.findByOwnerUsernameAndCategory(username, category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public byte[] exportVault(String username, String password) throws Exception {

        List<PasswordEntryResponse> entries = getAllEntries(username);
        String json = objectMapper.writeValueAsString(entries);
        String encrypted = encryptionUtil.encryptWithKey(json, password);
        return encrypted.getBytes();
    }

    public byte[] exportVaultCsv(String username) throws Exception {
        List<AllPasswordEntry> entries = repo.findByOwnerUsername(username);
        StringBuilder csv = new StringBuilder("accountName,website,username,passwordEncrypted,category,notes\n");
        for (AllPasswordEntry entry : entries) {
            csv.append(escapeCsv(entry.getAccountName())).append(",")
               .append(escapeCsv(entry.getWebsite())).append(",")
               .append(escapeCsv(entry.getUsername())).append(",")
               .append(escapeCsv(entry.getPasswordEncrypted())).append(",")
               .append(escapeCsv(entry.getCategory())).append(",")
               .append(escapeCsv(entry.getNotes())).append("\n");
        }
        return csv.toString().getBytes();
    }

//    public byte[] exportVault(String username) throws Exception {
//        List<AllPasswordEntry> entries = repo.findByOwnerUsername(username)
//                .stream()
//                .map(p -> {
//                    AllPasswordEntry dto = new AllPasswordEntry();
//                    dto.setAccountName(p.getAccountName());
//                    dto.setWebsite(p.getWebsite());
//                    dto.setUsername(p.getUsername());
//                    dto.setPasswordEncrypted(p.getPasswordEncrypted());
//                    dto.setCategory(p.getCategory());
//
//                    dto.setNotes(p.getNotes());
//                    return dto;
//                }).collect(Collectors.toList());
//
//        String json = objectMapper.writeValueAsString(entries);
//        String encrypted = encryptionService.encrypt(json);
//        return encrypted.getBytes();
//    }

    public void importVaultCsv(String username, MultipartFile file) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        boolean header = true;

        while ((line = reader.readLine()) != null) {

            if (header) {
                header = false;
                continue;
            }

            // Parse CSV respecting quoted fields
            String[] data = parseCsvLine(line);
            if (data.length < 6) continue;

            AllPasswordEntry entry = new AllPasswordEntry();
            entry.setAccountName(data[0]);
            entry.setWebsite(data[1]);
            entry.setUsername(data[2]);
            entry.setPasswordEncrypted(data[3]); // already encrypted from export
            entry.setCategory(data[4]);
            entry.setNotes(data.length > 5 ? data[5] : "");
            entry.setOwnerUsername(username);
            entry.setCreatedAt(LocalDateTime.now());
            entry.setUpdatedAt(LocalDateTime.now());

            repo.save(entry);
        }
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Transactional
    public void importVault(String username, byte[] data, String password) throws Exception {

        String encrypted = new String(data);
        String json = encryptionUtil.decryptWithKey(encrypted, password);
        
        List<PasswordEntryRequest> entries = objectMapper.readValue(json, 
                new com.fasterxml.jackson.core.type.TypeReference<List<PasswordEntryRequest>>() {});

        for (PasswordEntryRequest req : entries) {
            addEntry(username, req);
        }
    }

    public java.util.Map<String, Object> getAuditReport(String username) throws Exception {
        List<PasswordEntryResponse> entries = getAllEntries(username);
        long weak = 0, medium = 0, strong = 0, veryStrong = 0;
        
        for (PasswordEntryResponse e : entries) {
            int score = calculateScore(e.getPassword());
            if (score <= 2) weak++;
            else if (score <= 4) medium++;
            else if (score == 5) strong++;
            else veryStrong++;
        }

        java.util.Map<String, Object> report = new java.util.HashMap<>();
        report.put("total", entries.size());
        report.put("weak", weak);
        report.put("medium", medium);
        report.put("strong", strong);
        report.put("veryStrong", veryStrong);
        report.put("timestamp", LocalDateTime.now());
        
        return report;
    }

    private int calculateScore(String p) {
        int score = 0;
        if (p == null) return 0;
        if (p.length() >= 8) score++;
        if (p.length() >= 12) score++;
        if (p.matches(".*[A-Z].*")) score++;
        if (p.matches(".*[a-z].*")) score++;
        if (p.matches(".*[0-9].*")) score++;
        if (p.matches(".*[!@#$%^&*()].*")) score++;
        return score;
    }

    private PasswordEntryResponse mapToResponse(
            AllPasswordEntry entry) {

        try {

            return PasswordEntryResponse.builder()
                    .id(entry.getId())
                    .accountName(entry.getAccountName())
                    .website(entry.getWebsite())
                    .username(entry.getUsername())
                    .password(
                            encryptionUtil.decrypt(
                                    entry.getPasswordEncrypted()))
                    .category(entry.getCategory())
                    .notes(entry.getNotes())
                    .favorite(entry.isFavorite())
                    .createdAt(entry.getCreatedAt())
                    .updatedAt(entry.getUpdatedAt())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
