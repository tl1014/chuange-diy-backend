package com.chuange.diy.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class DiyApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiyApplication.class, args);
    }

    // 上传和读取必须使用同一个目录；目录 URI 必须以 / 结尾。
    @Bean
    WebMvcConfigurer uploadResources(@Value("${app.upload-dir:uploads}") String uploadDir) {
        final String location;
        try {
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            location = directory.toUri().toString();
        } catch (Exception e) {
            throw new IllegalStateException("无法创建图片上传目录: " + uploadDir, e);
        }
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(location);
            }
        };
    }
}
