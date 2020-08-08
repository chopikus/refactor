package com.chopikus.manager_app;

public class SupplyProductsModel {

    String id="", idSupplier="", idCafe="", name="", expireDate="", code="";
    String amount="";
    String measurementUnit="";
    public SupplyProductsModel(String id, String idSupplier, String idCafe, String code, String name, String amount, String expireDate) {
        this.id = id;
        this.idSupplier = idSupplier;
        this.idCafe = idCafe;
        this.name = name;
        this.amount = amount;
        this.expireDate = expireDate;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(String idSupplier) {
        this.idSupplier = idSupplier;
    }

    public String getIdCafe() {
        return idCafe;
    }

    public void setIdCafe(String idCafe) {
        this.idCafe = idCafe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }
}