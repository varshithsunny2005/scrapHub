package com.example.ScrapHub.service;

import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Seller;
import com.example.ScrapHub.repo.ConsumerRepository;
import com.example.ScrapHub.repo.SellerRepository;
import com.example.ScrapHub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthService {

    @Autowired
    private ConsumerRepository consumerRepo;

    @Autowired
    private SellerRepository sellerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ---------------- Register ----------------
    public String registerConsumer(RegisterRequest request) {
        if (consumerRepo.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Consumer already exists");
        }

        Consumer c = new Consumer();
        c.setName(request.getName());
        c.setPhoneNumber(request.getPhoneNumber());
        c.setPassword(passwordEncoder.encode(request.getPassword()));
        c.setCompanyName(request.getCompanyName());
        c.setPincode(request.getPinCode());
        c.setRequiredProductPosts(new ArrayList<>());

        consumerRepo.save(c);
        return "Consumer registered successfully";
    }

    public String registerSeller(RegisterRequest request) {
        if (sellerRepo.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Seller already exists");
        }

        Seller s = new Seller();
        s.setName(request.getName());
        s.setPhoneNumber(request.getPhoneNumber());
        s.setPassword(passwordEncoder.encode(request.getPassword()));
        s.setPincode(request.getPinCode());
        s.setProductPosts(new ArrayList<>());

        sellerRepo.save(s);
        return "Seller registered successfully";
    }

    // ---------------- Login ----------------
    public String loginConsumer(LoginRequest request) {
        Consumer c = consumerRepo.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        if (!passwordEncoder.matches(request.getPassword(), c.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(c.getPhoneNumber(), "ROLE_CONSUMER");
    }

    public String loginSeller(LoginRequest request) {
        Seller s = sellerRepo.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!passwordEncoder.matches(request.getPassword(), s.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(s.getPhoneNumber(), "ROLE_SELLER");
    }

    // ---------------- Profile Fetching ----------------
    public Consumer getConsumerProfile(String phone) {
        return consumerRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("Consumer not found"));
    }

    public Seller getSellerProfile(String phone) {
        return sellerRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
    }
}