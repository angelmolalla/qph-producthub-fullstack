package com.example.demo.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

    @GetMapping("/api/auth/ping")
    public String authPing() {

        return "OK";
    }


    @GetMapping("/api/users/test")
    public String users() {

        return "OK";
    }


    @PostMapping("/api/sales")
    public String createSale() {

        return "OK";
    }


    @GetMapping("/api/sales/me")
    public String mySales() {

        return "OK";
    }


    @GetMapping("/api/sales/{id}")
    public String saleById() {

        return "OK";
    }


    @GetMapping("/api/sales")
    public String allSales() {

        return "OK";
    }


    @GetMapping("/api/products/test")
    public String products() {

        return "OK";
    }
}