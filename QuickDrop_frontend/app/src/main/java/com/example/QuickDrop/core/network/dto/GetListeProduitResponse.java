package com.example.QuickDrop.core.network.dto;
public class GetListeProduitResponse {
    int id;
    int stock_disponible;
    int article;

    int fournisseur;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStock_disponible() {
        return stock_disponible;
    }

    public void setStock_disponible(int stock_disponible) {
        this.stock_disponible = stock_disponible;
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(int fournisseur) {
        this.fournisseur = fournisseur;
    }
}
