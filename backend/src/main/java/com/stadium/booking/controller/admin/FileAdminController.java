package com.stadium.booking.controller.admin;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.response.FileUploadResponse;
import com.stadium.booking.security.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Tag(name = "文件管理", description = "后台文件上传接口")
@RestController
@RequestMapping("/admin/files")
public class FileAdminController {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    @Operation(summary = "上传球馆图片")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("venue:update")
    public Result<FileUploadResponse> uploadVenueImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "图片大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "仅支持 jpg、jpeg、png、webp 格式");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "仅支持 jpg、jpeg、png、webp 格式");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path uploadDir = resolveUploadRoot().resolve("venue");
        Path targetPath = uploadDir.resolve(fileName);

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "图片上传失败");
        }

        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setUrl("/api/uploads/venue/" + fileName);
        response.setSize(file.getSize());
        return Result.success(response);
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "文件名无效");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private Path resolveUploadRoot() {
        Path basePath = Paths.get("").toAbsolutePath().normalize();
        if (basePath.getFileName() != null && "backend".equalsIgnoreCase(basePath.getFileName().toString())) {
            Path parent = basePath.getParent();
            if (parent != null) {
                basePath = parent;
            }
        }
        return basePath.resolve("uploads").normalize();
    }
}
