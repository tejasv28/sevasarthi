package com.sevasarthi.backend.dto;

import com.sevasarthi.backend.models.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private User user;
    private String accessToken;
    private String refreshToken;
    private String providerStatus;
}
