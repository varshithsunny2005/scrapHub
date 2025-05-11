package com.example.ScrapHub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Offer;
import com.example.ScrapHub.model.ProductPost;
import com.example.ScrapHub.model.Seller;
import com.example.ScrapHub.repo.ConsumerRepository;
import com.example.ScrapHub.repo.SellerRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SellerRepository sellerRepository;

    // Consumer making an offer to a Seller's product
    @PostMapping("/seller/{productId}/{sellerId}")
    public ResponseEntity<?> makeOfferToSeller(
            @PathVariable String productId,
            @PathVariable String sellerId,
            @RequestBody Offer offer) {

        try {
            Optional<Seller> sellerOpt = sellerRepository.findById(sellerId);
            if (sellerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Seller not found");
            }

            Seller seller = sellerOpt.get();

            ProductPost targetProduct = seller.getProductPosts()
                    .stream()
                    .filter(post -> post.getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (targetProduct == null) {
                return ResponseEntity.badRequest().body("Product not found");
            }

            targetProduct.addOffer(offer);
            sellerRepository.save(seller);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Offer submitted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting offer: " + e.getMessage());
        }
    }

    // Seller making an offer to a Consumer's required product
    @PostMapping("/consumer/{productId}/{consumerId}")
    public ResponseEntity<?> makeOfferToConsumer(
            @PathVariable String productId,
            @PathVariable String consumerId,
            @RequestBody Offer offer) {

        try {
            Optional<Consumer> consumerOpt = consumerRepository.findById(consumerId);
            if (consumerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Consumer not found");
            }

            Consumer consumer = consumerOpt.get();

            ProductPost targetProduct = consumer.getRequiredProductPosts()
                    .stream()
                    .filter(post -> post.getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (targetProduct == null) {
                return ResponseEntity.badRequest().body("Required product not found");
            }

            targetProduct.addOffer(offer);
            consumerRepository.save(consumer);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Offer submitted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting offer: " + e.getMessage());
        }
    }
    
}
