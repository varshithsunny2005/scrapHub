package com.example.ScrapHub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductPost {
    
    private String id;
    @TextIndexed
    private String wasteName;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isKgs() {
        return isKgs;
    }

    private int noOf;
    private boolean isKgs;
    private float price;
    private List<Offer> offersReceived;
    
    public ProductPost() {
         this.id = UUID.randomUUID().toString();
        this.offersReceived = new ArrayList<>();
    }

    public ProductPost(String wasteName, int noOf, boolean isKgs, float price) {
        this.id = UUID.randomUUID().toString();
        this.wasteName = wasteName;
        this.noOf = noOf;
        this.isKgs = isKgs;
        this.price = price;
        this.offersReceived = new ArrayList<>();
    }
    
    public List<Offer> getOffersReceived() {
        return offersReceived;
    }

    public void setOffersReceived(List<Offer> offersReceived) {
        this.offersReceived = offersReceived;
    }
    
    public void addOffer(Offer offer) {
        if (this.offersReceived == null) {
            this.offersReceived = new ArrayList<>();
        }
        this.offersReceived.add(offer);
    }

    public String getWasteName() {
        return wasteName;
    }

    public void setWasteName(String wasteName) {
        this.wasteName = wasteName;
    }

    public int getNoOf() {
        return noOf;
    }

    public void setNoOf(int noOf) {
        this.noOf = noOf;
    }

    public boolean getKgs() {
        return isKgs;
    }

    public void setKgs(boolean isKgs) {
        this.isKgs = isKgs;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}