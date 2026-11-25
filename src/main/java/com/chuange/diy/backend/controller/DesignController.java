package com.chuange.diy.backend.controller;

import com.chuange.diy.backend.model.dto.DesignRequest;
import com.chuange.diy.backend.model.dto.DesignResponse;
import com.chuange.diy.backend.service.DesignService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService) {
        this.designService = designService;
    }

    @PostMapping("/designs")
    public DesignResponse save(@RequestBody DesignRequest request) {
        return designService.saveDesign(request);
    }

    @GetMapping("/designs/user/{userId}")
    public List<DesignResponse> byUser(@PathVariable Long userId) {
        return designService.listByUserId(userId);
    }

    @GetMapping("/admin/designs")
    public List<DesignResponse> allForAdmin() {
        return designService.listAllForAdmin();
    }
}