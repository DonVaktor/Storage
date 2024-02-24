package com.example.com.Models;

public class Box {
    private final String barcode;
    private String name;
    private String quantity;



    public Box(String barcode, String name, String quantity) {
        this.barcode = barcode;
        this.name = name;
        this.quantity = quantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }
}