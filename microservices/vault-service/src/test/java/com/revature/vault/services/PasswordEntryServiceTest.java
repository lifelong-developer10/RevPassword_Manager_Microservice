package com.revature.vault.services;

import com.revature.vault.dtos.PasswordEntryRequest;
import com.revature.vault.dtos.PasswordEntryResponse;
import com.revature.vault.models.AllPasswordEntry;
import com.revature.vault.repository.PasswordEntryRepository;
import com.revature.vault.security.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.revature.vault.client.NotificationClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordEntryServiceTest {

    @Mock
    private PasswordEntryRepository repo;
    
    @Mock
    private EncryptionUtil encryptionUtil;
    
    @Mock
    private EncryptionService encryptionService;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private PasswordEntryService service;

    private PasswordEntryRequest request;

    @BeforeEach
    void setUp() {
        request = new PasswordEntryRequest();
        request.setAccountName("Google");
        request.setWebsite("google.com");
        request.setUsername("user123");
        request.setPassword("pass123");
    }

    @Test
    void addEntry_ShouldSaveAndReturnResponse() throws Exception {
        when(encryptionUtil.encrypt(anyString())).thenReturn("encryptedPass");
        
        PasswordEntryResponse response = service.addEntry("owner", request);
        
        assertNotNull(response);
        assertEquals("Google", response.getAccountName());
        verify(repo, times(1)).save(any(AllPasswordEntry.class));
    }

    @Test
    void getEntry_ShouldReturnResponse_WhenExists() throws Exception {
        AllPasswordEntry entry = new AllPasswordEntry();
        entry.setId(1L);
        entry.setAccountName("Test");
        entry.setOwnerUsername("owner");
        entry.setPasswordEncrypted("enc");

        when(repo.findById(1L)).thenReturn(Optional.of(entry));
        when(encryptionUtil.decrypt("enc")).thenReturn("decrypted");

        PasswordEntryResponse res = service.getEntry(1L);
        
        assertNotNull(res);
        assertEquals("decrypted", res.getPassword());
    }
}
