package com.example.ScrapHub.repo;

import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Seller;

import java.util.List;

import org.springframework.stereotype.Repository;
@Repository
public interface searchRepo {
    List<Consumer> findBySeller(String text);
    List<Seller> findByConsumers(String text);
}
