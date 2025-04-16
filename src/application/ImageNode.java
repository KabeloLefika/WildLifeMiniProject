package application;

import javafx.scene.paint.Color;

public class ImageNode extends Node {
    private final double cost;
    private String type; // e.g., "water", "forest", "land"
    private Color color;
    
    
    public ImageNode(double x, double y, double cost, String type) {
        super(x, y);
        this.cost = cost;
        this.type = type;
    }

    public double getCost() {
        return cost;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) { 
        this.type = type;
        // Add any type-specific logic here
    }
}

