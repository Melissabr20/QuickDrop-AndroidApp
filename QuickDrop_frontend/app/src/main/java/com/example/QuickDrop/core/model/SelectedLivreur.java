package com.example.QuickDrop.core.model;
public class SelectedLivreur {

    private int livreur;

    private int distance;
    private String duration ;


    public int getDistance() {
        return distance;
    }

    public int getLivreur() {
        return livreur;
    }

    public void setLivreur(int livreur) {
        this.livreur = livreur;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) { this.duration = duration;}
    public void setDistance(int distance) {
        this.distance = distance;
    }
}
