package com.stadium.booking.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QrCodeResponse {
    private String token;
    private String qrData;
    private LocalDateTime expiresAt;
}
