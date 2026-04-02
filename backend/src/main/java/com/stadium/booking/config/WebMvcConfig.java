package com.stadium.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = resolveUploadRoot();
        String location = uploadPath.toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
        registry.addResourceHandler("/api/uploads/**").addResourceLocations(location);
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
