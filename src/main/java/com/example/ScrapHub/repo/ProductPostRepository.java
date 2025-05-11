package com.example.ScrapHub.repo;

import com.example.ScrapHub.model.ProductPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProductPostRepository extends MongoRepository<ProductPost, String> {
}
