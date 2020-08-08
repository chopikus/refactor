package com.chopikus.manager_app;

import android.graphics.Bitmap;

public class DataModel {

    String name;
    int id_;
    String imageUrl;
    Double price;

    String minutes;
    String productsJSON="";
    String description="";
    String category="";
    int amount=0;
    String unit="";
    public DataModel(String name, Double price, int id_, String imageUrl, int amount, String measurement_unit, String minutes, String productsJSON) {
        this.name = name;
        this.price = price;
        this.id_ = id_;
        this.imageUrl = imageUrl;
        this.amount = amount;
        this.unit = measurement_unit;
        this.minutes = minutes;
        this.productsJSON = productsJSON;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductsJSON() {
        return productsJSON;
    }

    public void setProductsJSON(String productsJSON) {
        this.productsJSON = productsJSON;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId_() {
        return id_;
    }

    public void setId_(int id_) {
        this.id_ = id_;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}