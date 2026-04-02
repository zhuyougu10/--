package com.stadium.booking.dto.response;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileName;
    private String url;
    private Long size;
}
