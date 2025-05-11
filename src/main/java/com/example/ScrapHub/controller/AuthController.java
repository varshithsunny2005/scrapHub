package com.example.ScrapHub.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Seller;
import com.example.ScrapHub.repo.ConsumerRepository;
import com.example.ScrapHub.repo.SellerRepository;
import com.example.ScrapHub.service.AuthService;
import com.example.ScrapHub.service.LoginRequest;
import com.example.ScrapHub.service.RegisterRequest;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private ConsumerRepository consumerRepository;
       
    @PostMapping("/register-consumer")
    public ResponseEntity<String> registerConsumer(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerConsumer(request));
    }

    @PostMapping("/register-seller")
    public ResponseEntity<String> registerSeller(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerSeller(request));
    }

    @PostMapping("/login-consumer")
    public ResponseEntity<String> loginConsumer(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginConsumer(request));
    }

    @PostMapping("/login-seller")
    public ResponseEntity<String> loginSeller(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginSeller(request));
    }

    @GetMapping("/consumer/{phone}")
    public ResponseEntity<Optional<Consumer>> getConsumer(@PathVariable String phone) {
        return ResponseEntity.ok(consumerRepository.findByPhoneNumber(phone));
    }

    @GetMapping("/seller/{phone}")
    public ResponseEntity<Optional<Seller>> getSeller(@PathVariable String phone) {
        return ResponseEntity.ok(sellerRepository.findByPhoneNumber(phone));
    }

}
