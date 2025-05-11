package com.example.ScrapHub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Seller;
import com.example.ScrapHub.repo.searchRepo;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private searchRepo repo;

    @GetMapping("/sellers/{text}")
    public ResponseEntity<List<Consumer>> searchBySellers(@PathVariable String text) {
        return ResponseEntity.ok(repo.findBySeller(text));
    }

    @GetMapping("/consumers/{text}")
    public ResponseEntity<List<Seller>> searchByConsumers(@PathVariable String text) {
        return ResponseEntity.ok(repo.findByConsumers(text));
    }

    // ✅ Generic search endpoint matching frontend usage
    @GetMapping
    public ResponseEntity<?> searchByRole(
            @RequestParam String keyword,
            @RequestParam String role) {

        if (role.equalsIgnoreCase("seller")) {
            // Sellers search for consumers
            return ResponseEntity.ok(repo.findBySeller(keyword));
        } else if (role.equalsIgnoreCase("consumer")) {
            // Consumers search for sellers
            return ResponseEntity.ok(repo.findByConsumers(keyword));
           
        } else {
            return ResponseEntity.badRequest().body("Invalid role parameter.");
        }
    }
}
