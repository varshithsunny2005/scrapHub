package com.example.ScrapHub.repo;

import com.example.ScrapHub.model.Consumer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerRepository extends MongoRepository<Consumer, String> {
    Optional<Consumer> findByPhoneNumber(String phoneNumber);
    @Query("{ $text: { $search: ?0 } }")
List<Consumer> searchByKeyword(String keyword);

}
