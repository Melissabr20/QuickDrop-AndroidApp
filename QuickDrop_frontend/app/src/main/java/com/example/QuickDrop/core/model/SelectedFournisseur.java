package com.example.QuickDrop.core.model;
public class SelectedFournisseur {

    private int fournisseur;

    public int getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(int fournisseur) {
        this.fournisseur = fournisseur;
    }

    public int getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private int distance;
    private String duration ;

}
