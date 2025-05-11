package com.example.ScrapHub.repo;

import com.example.ScrapHub.model.Seller;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
@Repository
public interface SellerRepository extends MongoRepository<Seller, String> {
    Optional<Seller> findByPhoneNumber(String phoneNumber);
    @Query("{ $text: { $search: ?0 } }")
List<Seller> searchByKeyword(String keyword);

}
