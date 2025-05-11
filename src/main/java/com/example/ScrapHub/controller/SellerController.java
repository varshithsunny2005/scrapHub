package com.example.ScrapHub.controller;

import com.example.ScrapHub.model.AcceptedOffer;
import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Offer;
import com.example.ScrapHub.model.ProductPost;
import com.example.ScrapHub.model.Seller;
import com.example.ScrapHub.repo.ConsumerRepository;
import com.example.ScrapHub.repo.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    @Autowired
    private SellerRepository repo;
    @Autowired
    private ConsumerRepository consumerRepository;

    @GetMapping("/get")
    public ResponseEntity<?> getSeller(@RequestParam String phoneNumber) {
        Optional<Seller> sellerOpt = repo.findByPhoneNumber(phoneNumber);

        if (sellerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Fetch seller with product posts and offers
        Seller seller = sellerOpt.get();

        // Make sure to initialize the collections if they're null
        if (seller.getProductPosts() == null) {
            seller.setProductPosts(new ArrayList<>());
        } else {
            // For each product post, initialize offers if null
            seller.getProductPosts().forEach(product -> {
                if (product.getOffersReceived() == null) {
                    product.setOffersReceived(new ArrayList<>());
                }
            });
        }

        return ResponseEntity.ok(seller);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateSeller(@RequestBody Seller updatedSeller) {
        Optional<Seller> existingSeller = repo.findByPhoneNumber(updatedSeller.getPhoneNumber());
        if (existingSeller.isPresent()) {
            Seller seller = existingSeller.get();

            if (updatedSeller.getName() != null)
                seller.setName(updatedSeller.getName());
            if (updatedSeller.getPincode() != null && updatedSeller.getPincode() > 0)
                seller.setPincode(updatedSeller.getPincode());
            if (updatedSeller.getPassword() != null && !updatedSeller.getPassword().isEmpty())
                seller.setPassword(updatedSeller.getPassword());
            if (updatedSeller.getProductPosts() != null)
                seller.setProductPosts(updatedSeller.getProductPosts());

            repo.save(seller);
            return ResponseEntity.ok("Seller updated successfully");
        }

        return ResponseEntity.status(404).body("Seller not found");
    }

    @GetMapping("/offers")
    public ResponseEntity<?> getSellerOffers(@RequestParam String phoneNumber) {
        Optional<Seller> sellerOpt = repo.findByPhoneNumber(phoneNumber);

        if (sellerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seller seller = sellerOpt.get();

        // Create list to hold offers with product information
        List<Map<String, Object>> offersWithProductInfo = new ArrayList<>();

        // For each product post, add product info to offers
        for (ProductPost product : seller.getProductPosts()) {
            if (product.getOffersReceived() != null) {
                for (Offer offer : product.getOffersReceived()) {
                    Map<String, Object> offerWithProduct = new HashMap<>();
                    offerWithProduct.put("offer", offer);

                    // Add needed product details
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("id", product.getId());
                    productInfo.put("wasteName", product.getWasteName());
                    productInfo.put("price", product.getPrice());
                    productInfo.put("isKgs", product.getKgs());

                    offerWithProduct.put("productPost", productInfo);
                    offersWithProductInfo.add(offerWithProduct);
                }
            }
        }

        return ResponseEntity.ok(offersWithProductInfo);
    }

    @PostMapping("/accept-offer")
    public ResponseEntity<?> acceptOffer(@RequestBody Map<String, Object> requestBody) {
        String phoneNumber = (String) requestBody.get("phoneNumber");
        Integer productIndex = (Integer) requestBody.get("productIndex");
        String counterPartyPhone = (String) requestBody.get("counterPartyPhone");
        Integer quantity = requestBody.get("quantity") instanceof Integer ? (Integer) requestBody.get("quantity")
                : Integer.valueOf(requestBody.get("quantity").toString());
        Integer price = requestBody.get("price") instanceof Integer ? (Integer) requestBody.get("price")
                : Integer.valueOf(requestBody.get("price").toString());

        // Validate required parameters
        if (phoneNumber == null || counterPartyPhone == null || productIndex == null || quantity == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        Optional<Seller> sellerOpt = repo.findByPhoneNumber(phoneNumber);
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Seller not found");
        }

        Seller seller = sellerOpt.get();
        List<ProductPost> productPosts = seller.getProductPosts();

        // Check if product index is valid
        if (productPosts == null || productIndex >= productPosts.size()) {
            return ResponseEntity.badRequest().body("Invalid product index");
        }

        ProductPost product = productPosts.get(productIndex);
        List<Offer> offers = product.getOffersReceived();

        // Find the offer from the counter party
        Optional<Offer> offerOpt = offers.stream()
                .filter(o -> counterPartyPhone.equals(o.getReqPhoneNumber()))
                .findFirst();

        if (offerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Offer not found");
        }

        Offer acceptedOffer = offerOpt.get();

        // Check if there is enough quantity available
        int currentQuantity = product.getNoOf();
        int requestedQuantity = acceptedOffer.getReqQuantity();

        if (currentQuantity < requestedQuantity) {
            return ResponseEntity.badRequest().body("Not enough quantity available");
        }

        // Calculate remaining quantity after accepting this offer
        int remainingQuantity = currentQuantity - requestedQuantity;

        // Create accepted offer record
        AcceptedOffer newAcceptedOffer = new AcceptedOffer();
        newAcceptedOffer.setProductId(product.getId());
        newAcceptedOffer.setWasteName(product.getWasteName());
        newAcceptedOffer.setQuantity(requestedQuantity);
        newAcceptedOffer.setPrice(acceptedOffer.getReqPrice());
        newAcceptedOffer.setSellerPhoneNumber(phoneNumber);
        newAcceptedOffer.setBuyerPhoneNumber(counterPartyPhone);
        newAcceptedOffer.setSellerName(seller.getName());

        // Add the accepted offer to the seller
        seller.addAcceptedOffer(newAcceptedOffer);

        // Now, find the consumer to add the accepted offer to their records too
        Optional<Consumer> consumerOpt = consumerRepository.findByPhoneNumber(counterPartyPhone);
        if (consumerOpt.isPresent()) {
            Consumer consumer = consumerOpt.get();
            // Create a copy of the offer for the consumer
            AcceptedOffer consumerOffer = new AcceptedOffer();
            consumerOffer.setId(newAcceptedOffer.getId()); // Use same ID to link them
            consumerOffer.setProductId(product.getId());
            consumerOffer.setWasteName(product.getWasteName());
            consumerOffer.setQuantity(requestedQuantity);
            consumerOffer.setPrice(acceptedOffer.getReqPrice());
            consumerOffer.setSellerPhoneNumber(phoneNumber);
            consumerOffer.setBuyerPhoneNumber(counterPartyPhone);
            consumerOffer.setSellerName(seller.getName());
            consumerOffer.setBuyerName(consumer.getName());
            newAcceptedOffer.setBuyerName(consumer.getName()); // Update seller's record with buyer name

            consumer.addAcceptedOffer(consumerOffer);
            consumerRepository.save(consumer);
        }

        // If the accepted offer uses all available quantity, remove the product
        // Otherwise, just update the quantity
        if (remainingQuantity == 0) {
            // Remove the product completely from the list
            productPosts.remove(productIndex);
        } else {
            // Update the product quantity
            product.setNoOf(remainingQuantity);

            // Remove the accepted offer from offers list
            offers.remove(acceptedOffer);
        }

        // Save the updated seller data
        repo.save(seller);

        String message = remainingQuantity == 0
                ? "Offer accepted successfully. Product has been removed as all quantity has been sold."
                : "Offer accepted successfully. Product quantity updated.";

        return ResponseEntity.ok(message);
    }

    @PostMapping("/reject-offer")
    public ResponseEntity<?> rejectOffer(@RequestBody Map<String, Object> requestBody) {
        String phoneNumber = (String) requestBody.get("phoneNumber");
        Integer productIndex = (Integer) requestBody.get("productIndex");
        String counterPartyPhone = (String) requestBody.get("counterPartyPhone");

        // Validate required parameters
        if (phoneNumber == null || counterPartyPhone == null || productIndex == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        Optional<Seller> sellerOpt = repo.findByPhoneNumber(phoneNumber);
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Seller not found");
        }

        Seller seller = sellerOpt.get();
        List<ProductPost> productPosts = seller.getProductPosts();

        // Check if product index is valid
        if (productPosts == null || productIndex >= productPosts.size()) {
            return ResponseEntity.badRequest().body("Invalid product index");
        }

        ProductPost product = productPosts.get(productIndex);
        List<Offer> offers = product.getOffersReceived();

        // Find the offer from the counter party
        Optional<Offer> offerOpt = offers.stream()
                .filter(o -> counterPartyPhone.equals(o.getReqPhoneNumber()))
                .findFirst();

        if (offerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Offer not found");
        }

        // Remove the rejected offer
        offers.remove(offerOpt.get());
        repo.save(seller);

        return ResponseEntity.ok("Offer rejected successfully");
    }

    @GetMapping("/accepted-offers")
    public ResponseEntity<?> getAcceptedOffers(@RequestParam String phoneNumber) {
        Optional<Seller> sellerOpt = repo.findByPhoneNumber(phoneNumber);

        if (sellerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seller seller = sellerOpt.get();
        List<AcceptedOffer> acceptedOffers = seller.getAcceptedOffers();

        // Sort by date, newest first
        if (acceptedOffers != null) {
            acceptedOffers.sort((o1, o2) -> o2.getAcceptedDate().compareTo(o1.getAcceptedDate()));
        } else {
            acceptedOffers = new ArrayList<>();
        }

        return ResponseEntity.ok(acceptedOffers);
    }

    @PostMapping("/update-offer-status")
    public ResponseEntity<?> updateOfferStatus(
            @RequestParam String phoneNumber,
            @RequestParam String offerId,
            @RequestParam String status) {

        if (!status.equals("PENDING") && !status.equals("COMPLETED") && !status.equals("CANCELLED")) {
            return ResponseEntity.badRequest().body("Invalid status. Must be PENDING, COMPLETED, or CANCELLED");
        }

        Optional<Seller> sellerOpt = repo.findByPhoneNumber(phoneNumber);
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seller seller = sellerOpt.get();
        List<AcceptedOffer> acceptedOffers = seller.getAcceptedOffers();

        if (acceptedOffers == null) {
            return ResponseEntity.status(404).body("No accepted offers found");
        }

        boolean updated = false;
        for (AcceptedOffer offer : acceptedOffers) {
            if (offer.getId().equals(offerId)) {
                offer.setStatus(status);
                updated = true;
                break;
            }
        }

        if (!updated) {
            return ResponseEntity.status(404).body("Offer not found");
        }

        // Update the buyer's copy of the offer too
        Optional<Consumer> consumerOpt = consumerRepository.findByPhoneNumber(
                acceptedOffers.stream()
                        .filter(o -> o.getId().equals(offerId))
                        .findFirst()
                        .get()
                        .getBuyerPhoneNumber());

        if (consumerOpt.isPresent()) {
            Consumer consumer = consumerOpt.get();
            List<AcceptedOffer> consumerOffers = consumer.getAcceptedOffers();

            if (consumerOffers != null) {
                for (AcceptedOffer offer : consumerOffers) {
                    if (offer.getId().equals(offerId)) {
                        offer.setStatus(status);
                        break;
                    }
                }
                consumerRepository.save(consumer);
            }
        }

        repo.save(seller);
        return ResponseEntity.ok("Offer status updated successfully");
    }

}
