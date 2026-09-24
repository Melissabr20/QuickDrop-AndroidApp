package com.example.QuickDrop.core.model;
public class OrderLineItem {
    private String name,price,qte;
    private int id_order;

    public int getId_order() {
        return id_order;
    }

    public void setId_order(int id_order) {
        this.id_order = id_order;
    }

    public OrderLineItem(String name, String qte, String price,int id_order) {
        this.name = name;
        this.qte = qte;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQte() {
        return qte;
    }

    public void setQte(String qte) {
        this.qte = qte;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
