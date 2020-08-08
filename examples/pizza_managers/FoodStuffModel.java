package com.chopikus.manager_app;

public class FoodStuffModel {

    String id = "", code = "", measurement = "", name = "";
    String category_name = "";

    public FoodStuffModel(String id, String name, String code, String measurement, String category_name) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.measurement = measurement;
        this.category_name = category_name;
    }

    public String getCategoryName() {
        return category_name;
    }

    public void setCategoryName(String category_name) {
        this.category_name = category_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }
}