package com.example.QuickDrop.core.model;
import java.util.List;

public class DeliveryStop {
    private String title;
    private String text;
    private List<Integer>  id_order;
    private String type;


    public DeliveryStop(String title, String text, List<Integer> id_order, String type) {
        this.id_order = id_order;
        this.title = title;
        this.text = text;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Integer> getId_order() {
        return id_order;
    }

    public void setId_order(List<Integer> id_order) {
        this.id_order = id_order;
    }
}
