package com.chopikus.manager_app;

public class DishProductsModel {

    String code="";
    int amount=0;
    String name="";
    String measurementUnit="";
    public DishProductsModel(String name, String code, int amount, String measurementUnit) {
        this.code = code;
        this.amount = amount;
        this.name = name;
        this.measurementUnit = measurementUnit;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}