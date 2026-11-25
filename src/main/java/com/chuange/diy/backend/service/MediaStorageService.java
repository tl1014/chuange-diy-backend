package com.chuange.diy.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final Path rootUploadDir;

    public MediaStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.rootUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择图片");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片不能超过 8MB");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只允许 JPG、PNG、WEBP、GIF 图片");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "读取图片失败");
        }

        if (content.length == 0 || content.length > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片大小不合法");
        }

        if (!"image/webp".equals(contentType)) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
                BufferedImage img = ImageIO.read(bais);
                if (img == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的图片文件内容");
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片解析失败");
            }
        }

        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };

        String safeSubFolder = (folder == null || folder.isBlank())
                ? "misc"
                : folder.replaceAll("[^A-Za-z0-9_-]", "");
        if (safeSubFolder.isBlank()) {
            safeSubFolder = "misc";
        }

        Path targetDir = rootUploadDir.resolve(safeSubFolder).normalize();
        if (!targetDir.startsWith(rootUploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法存储路径");
        }

        String fileName = UUID.randomUUID() + ext;
        Path targetFile = targetDir.resolve(fileName).normalize();
        try {
            Files.createDirectories(targetDir);
            Files.write(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "保存图片失败");
        }

        return "/uploads/" + safeSubFolder + "/" + fileName;
    }
}