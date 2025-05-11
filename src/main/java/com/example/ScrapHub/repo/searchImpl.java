package com.example.ScrapHub.repo;

import com.example.ScrapHub.model.Consumer;
import com.example.ScrapHub.model.Seller;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class searchImpl implements searchRepo {

    @Autowired
    private MongoClient client;

    @Autowired
    private MongoConverter converter;

    // Search for Sellers
    @Override
    public List<Seller> findByConsumers(String text) {
        MongoDatabase database = client.getDatabase("scraphub");
        MongoCollection<Document> collection = database.getCollection("seller");

        // Use wildcard search to match text across all fields
        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
            new Document("$search", 
                new Document("index", "default")
                        .append("text", 
                new Document("query", text)
                            .append("path", 
                new Document("wildcard", "*")))),
            new Document("$limit", 10),
            new Document("$project", new Document("_id", 1)
                .append("phoneNumber", 1)
                .append("name", 1)
                .append("pincode", 1)
                .append("productPosts", 1))
        ));

        List<Seller> sellers = new ArrayList<>();
        result.forEach(doc -> sellers.add(converter.read(Seller.class, doc)));
        return sellers;
    }

    // Search for Consumers
    @Override
    public List<Consumer> findBySeller(String text) {
        MongoDatabase database = client.getDatabase("scraphub");
        MongoCollection<Document> collection = database.getCollection("consumer");
        
        // Use wildcard search to match text across all fields
        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
            new Document("$search", 
                new Document("index", "default")
                        .append("text", 
                new Document("query", text)
                            .append("path", 
                new Document("wildcard", "*")))),
            new Document("$limit", 10),
            new Document("$project", new Document("_id", 1)
                .append("phoneNumber", 1)
                .append("name", 1)
                .append("companyName", 1)
                .append("pincode", 1)
                .append("requiredProductPosts", 1))
        ));

        List<Consumer> consumers = new ArrayList<>();
        result.forEach(doc -> consumers.add(converter.read(Consumer.class, doc)));
        return consumers;
    }
}