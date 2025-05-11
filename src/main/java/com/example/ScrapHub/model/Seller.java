package com.example.ScrapHub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "seller")
public class Seller {
      @Id
    private String id;
    public String getId() {
        return id;
    }

      public void setId(String id) {
          this.id = id;
      }

    @Indexed(unique = true)
    private String phoneNumber;
    @TextIndexed
    private String name;
    
    @TextIndexed
    private Integer pincode;
    @Field("productPosts")
    private List<ProductPost> productPosts;
    private String password;
    

    @Field("AccepetedOffer")
    private List<AcceptedOffer> acceptedOffers;

    public List<AcceptedOffer> getAcceptedOffers() {
        if (acceptedOffers == null) {
            acceptedOffers = new ArrayList<>();
        }
        return acceptedOffers;
    }

    public void setAcceptedOffers(List<AcceptedOffer> acceptedOffers) {
        this.acceptedOffers = acceptedOffers;
    }

    public void addAcceptedOffer(AcceptedOffer offer) {
        if (acceptedOffers == null) {
            acceptedOffers = new ArrayList<>();
        }
        acceptedOffers.add(offer);
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPincode() {
        return pincode;
    }

    public void setPincode(Integer pincode) {
        this.pincode = pincode;
    }

    public List<ProductPost> getProductPosts() {
        return productPosts;
    }

    public void setProductPosts(List<ProductPost> productPosts) {
        this.productPosts = productPosts;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
