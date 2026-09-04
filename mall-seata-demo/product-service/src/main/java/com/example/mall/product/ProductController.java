package com.example.mall.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/product")
public class ProductController {
    private final ProductTccAction tcc;
    private final ProductService service;

    public ProductController(ProductTccAction tcc, ProductService service) {
        this.tcc = tcc;
        this.service = service;
    }

    @PostMapping("/tcc/prepare")
    public boolean prepare(@RequestBody BranchRequest request) {
        return tcc.prepare(null, request.getOrderId(), request.getResourceId(), request.getAmount());
    }

    @PostMapping("/saga/deduct")
    public boolean deduct(@RequestBody BranchRequest request) {
        return service.sagaDeduct(request.getOrderId(), request.getResourceId(), request.getAmount());
    }

    @PostMapping("/saga/restore/{orderId}")
    public boolean restore(@PathVariable String orderId) {
        return service.sagaRestore(orderId);
    }

    @GetMapping("/stock/{productId}")
    public int stock(@PathVariable String productId) {
        return service.available(productId);
    }
}
