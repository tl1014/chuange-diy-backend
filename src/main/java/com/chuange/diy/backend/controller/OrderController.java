package com.chuange.diy.backend.controller;

import com.chuange.diy.backend.model.dto.OrderRequest;
import com.chuange.diy.backend.model.dto.OrderResponse;
import com.chuange.diy.backend.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public OrderResponse create(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse detail(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/admin/orders")
    public List<OrderResponse> allForAdmin() {
        return orderService.listAllForAdmin();
    }

    @PostMapping("/orders/{id}/pay")
    public OrderResponse pay(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PostMapping("/admin/orders/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}