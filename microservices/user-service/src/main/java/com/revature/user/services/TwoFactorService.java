package com.revature.user.services;

import com.revature.user.models.MasterUser;
import com.revature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TwoFactorService {

    private final UserRepository userRepo;

    public String updateTwoFactor(String username, boolean enabled) {
        System.out.println("UPDATING 2FA for user: " + username + " to " + enabled);
        MasterUser user =
                userRepo.findByUsername(username).orElseThrow();

        user.setTwoFactorEnabled(enabled);

        userRepo.save(user);
        System.out.println("2FA UPDATED successfully");
        return enabled ?
                "2FA Enabled Successfully" :
                "2FA Disabled Successfully";
    }

    public boolean isTwoFactorEnabled(String username) {

        MasterUser user =
                userRepo.findByUsername(username).orElseThrow();

        return user.isTwoFactorEnabled();
    }
}


