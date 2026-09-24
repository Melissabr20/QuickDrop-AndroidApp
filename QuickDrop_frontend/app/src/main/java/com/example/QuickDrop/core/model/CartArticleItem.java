package com.example.QuickDrop.core.model;
public class CartArticleItem {
    String id,qte,disc,prix;
    int idarticle,idliste;

    public int getIdliste() {
        return idliste;
    }

    public void setIdliste(int idliste) {
        this.idliste = idliste;
    }

    public CartArticleItem(int idarticle, int idliste, String id, String qte, String disc, String prix) {
        this.id = id;
        this.qte = qte;
        this.disc = disc;
        this.prix = prix;
        this.idarticle = idarticle;
        this.idliste=idliste;
    }

    public int getI() {
        return idarticle;
    }

    public void setI(int i) {
        this.idarticle = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQte() {
        return qte;
    }

    public void setQte(String qte) {
        this.qte = qte;
    }

    public String getDisc() {
        return disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getPrix() {
        return prix;
    }

    public void setPrix(String prix) {
        this.prix = prix;
    }
}
