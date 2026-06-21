package com.upeu.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 150)
    private String displayName;

    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 10)
    private String locale;

    @Size(max = 2000)
    private String preferences;
}
