package com.example.QuickDrop.core.model;
import java.util.List;

public class RoutingSolution {
    private List<RoutingNode> success;


    public List<RoutingNode> getNodes() {
        return success;
    }

    public void setNodes(List<RoutingNode> nodes) {
        this.success = nodes;
    }

}
