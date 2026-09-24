package com.example.QuickDrop.core.model;
public class DeliveryOrderLine {
    //lignePanierResponse
    private int article;
    private int quantity;
    private int livraison;
    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getLivraison() {
        return livraison;
    }

    public void setLivraison(int livraison) {
        this.livraison = livraison;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
