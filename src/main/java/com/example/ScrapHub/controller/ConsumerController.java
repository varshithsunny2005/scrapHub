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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/consumer")
@PreAuthorize("hasRole('CONSUMER')")
public class ConsumerController {

    @Autowired
    private ConsumerRepository repo;
    @Autowired
    private SellerRepository sellerRepository;

    @GetMapping("/get")
    public ResponseEntity<?> getConsumer(@RequestParam String phoneNumber) {
        return repo.findByPhoneNumber(phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateConsumer(@RequestBody Consumer updatedConsumer) {
        Optional<Consumer> existingConsumer = repo.findByPhoneNumber(updatedConsumer.getPhoneNumber());
        if (existingConsumer.isPresent()) {
            Consumer consumer = existingConsumer.get();

            if (updatedConsumer.getName() != null)
                consumer.setName(updatedConsumer.getName());
            if (updatedConsumer.getPincode() != null && updatedConsumer.getPincode() > 0)
                consumer.setPincode(updatedConsumer.getPincode());
            if (updatedConsumer.getCompanyName() != null)
                consumer.setCompanyName(updatedConsumer.getCompanyName());
            if (updatedConsumer.getPassword() != null && !updatedConsumer.getPassword().isEmpty())
                consumer.setPassword(updatedConsumer.getPassword());
            if (updatedConsumer.getRequiredProductPosts() != null)
                consumer.setRequiredProductPosts(updatedConsumer.getRequiredProductPosts());

            repo.save(consumer);
            return ResponseEntity.ok("Consumer updated successfully");
        }

        return ResponseEntity.notFound().build();
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

        Optional<Consumer> consumerOpt = repo.findByPhoneNumber(phoneNumber);
        if (consumerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Consumer not found");
        }

        Consumer consumer = consumerOpt.get();
        List<ProductPost> productPosts = consumer.getRequiredProductPosts();

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

        // Create accepted offer record
        AcceptedOffer newAcceptedOffer = new AcceptedOffer();
        newAcceptedOffer.setProductId(product.getId());
        newAcceptedOffer.setWasteName(product.getWasteName());
        newAcceptedOffer.setQuantity(requestedQuantity);
        newAcceptedOffer.setPrice(acceptedOffer.getReqPrice());
        newAcceptedOffer.setBuyerPhoneNumber(phoneNumber);
        newAcceptedOffer.setSellerPhoneNumber(counterPartyPhone);
        newAcceptedOffer.setBuyerName(consumer.getName());

        // Add the accepted offer to the consumer
        consumer.addAcceptedOffer(newAcceptedOffer);

        // Now, find the seller to add the accepted offer to their records too
        Optional<Seller> sellerOpt = sellerRepository.findByPhoneNumber(counterPartyPhone);
        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            // Create a copy of the offer for the seller
            AcceptedOffer sellerOffer = new AcceptedOffer();
            sellerOffer.setId(newAcceptedOffer.getId()); // Use same ID to link them
            sellerOffer.setProductId(product.getId());
            sellerOffer.setWasteName(product.getWasteName());
            sellerOffer.setQuantity(requestedQuantity);
            sellerOffer.setPrice(acceptedOffer.getReqPrice());
            sellerOffer.setBuyerPhoneNumber(phoneNumber);
            sellerOffer.setSellerPhoneNumber(counterPartyPhone);
            sellerOffer.setBuyerName(consumer.getName());
            sellerOffer.setSellerName(seller.getName());
            newAcceptedOffer.setSellerName(seller.getName()); // Update consumer's record with seller name

            seller.addAcceptedOffer(sellerOffer);
            sellerRepository.save(seller);
        }

        // Update the product quantity by subtracting the accepted offer quantity
        product.setNoOf(currentQuantity - requestedQuantity);

        // Remove the offer after acceptance
        offers.remove(acceptedOffer);

        // Save the updated consumer with adjusted product quantity
        repo.save(consumer);

        return ResponseEntity.ok("Offer accepted successfully. Product quantity updated.");
    }

    @GetMapping("/accepted-offers")
    public ResponseEntity<?> getAcceptedOffers(@RequestParam String phoneNumber) {
        Optional<Consumer> consumerOpt = repo.findByPhoneNumber(phoneNumber);

        if (consumerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Consumer consumer = consumerOpt.get();
        List<AcceptedOffer> acceptedOffers = consumer.getAcceptedOffers();

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

        Optional<Consumer> consumerOpt = repo.findByPhoneNumber(phoneNumber);
        if (consumerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Consumer consumer = consumerOpt.get();
        List<AcceptedOffer> acceptedOffers = consumer.getAcceptedOffers();

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

        // Update the seller's copy of the offer too
        Optional<Seller> sellerOpt = sellerRepository.findByPhoneNumber(
                acceptedOffers.stream()
                        .filter(o -> o.getId().equals(offerId))
                        .findFirst()
                        .get()
                        .getSellerPhoneNumber());

        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            List<AcceptedOffer> sellerOffers = seller.getAcceptedOffers();

            if (sellerOffers != null) {
                for (AcceptedOffer offer : sellerOffers) {
                    if (offer.getId().equals(offerId)) {
                        offer.setStatus(status);
                        break;
                    }
                }
                sellerRepository.save(seller);
            }
        }

        repo.save(consumer);
        return ResponseEntity.ok("Offer status updated successfully");
    }
}
