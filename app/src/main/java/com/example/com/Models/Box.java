package com.example.com.Models;

public class Box {
    private final String barcode;
    private String name;
    private String quantity;
    private String category;

    public String getCategory() { return category;}

    public void setCategory(String category) { this.category = category;}





    public Box(String barcode, String name, String quantity, String category) {
        this.barcode = barcode;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
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