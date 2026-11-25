package com.chuange.diy.backend.controller;

import com.chuange.diy.backend.service.MediaStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping("/image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "folder", required = false) String folder) {
        String url = mediaStorageService.storeImage(file, folder);
        return Map.of("url", url);
    }
}