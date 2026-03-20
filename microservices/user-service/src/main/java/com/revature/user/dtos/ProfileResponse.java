package com.revature.user.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private String username;
    private String email;
    private String phone;
    private boolean twoFactorEnabled;
}
