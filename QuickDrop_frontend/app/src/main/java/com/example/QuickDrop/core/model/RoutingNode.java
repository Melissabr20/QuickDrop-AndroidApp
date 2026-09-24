package com.example.QuickDrop.core.model;
public class RoutingNode {
    private String type;
    private int id,id_ord,capacity;
    private String cord;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_ord() {
        return id_ord;
    }

    public void setId_ord(int id_ord) {
        this.id_ord = id_ord;
    }

    public String getCord() {
        return cord;
    }

    public void setCord(String cord) {
        this.cord = cord;
    }
}
