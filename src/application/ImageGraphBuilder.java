package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class ImageGraphBuilder {
    // How many pixels to skip (increase for lower resolution graph)
    private static final int SAMPLE_STEP = 10;

    public static void buildGraph(Image image, Graph graph) {
        PixelReader pixelReader = image.getPixelReader();
        int imgWidth = (int) image.getWidth();
        int imgHeight = (int) image.getHeight();
        
        // Create a 2D array to store the nodes so we can reference neighbors
        ImageNode[][] nodes = new ImageNode[imgHeight / SAMPLE_STEP + 1][imgWidth / SAMPLE_STEP + 1];

        // Create nodes based on sampled pixels
        for (int y = 0; y < imgHeight; y += SAMPLE_STEP) {
            for (int x = 0; x < imgWidth; x += SAMPLE_STEP) {
                Color color = pixelReader.getColor(x, y);
                ClassificationResult result = classifyColor(color);
                ImageNode node = new ImageNode(x, y, result.cost, result.type);
                nodes[y / SAMPLE_STEP][x / SAMPLE_STEP] = node;
                graph.addNode(node);
            }
        }

        // Create edges between adjacent nodes (using 4-neighborhood: right and down)
        int rows = nodes.length;
        int cols = nodes[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ImageNode current = nodes[i][j];
                if (current == null) continue;

                // Connect with right neighbor
                if (j < cols - 1 && nodes[i][j + 1] != null) {
                    Edge edge = new Edge(current, nodes[i][j + 1]);
                    graph.addEdge(edge);
                }
                // Connect with bottom neighbor
                if (i < rows - 1 && nodes[i + 1][j] != null) {
                    Edge edge = new Edge(current, nodes[i + 1][j]);
                    graph.addEdge(edge);
                }
                // Optionally add diagonal connections if needed
            }
        }
    }

    // Helper class to hold classification results
    private static class ClassificationResult {
        double cost;
        String type;

        ClassificationResult(double cost, String type) {
            this.cost = cost;
            this.type = type;
        }
    }

    // Classify a pixel based on its color.
    private static ClassificationResult classifyColor(Color color) {
        // A simple heuristic based on the dominant color component
        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();
        
        if (color.getBlue() > 0.7 && color.getRed() < 0.3) {
            return new ClassificationResult(Double.POSITIVE_INFINITY, "water");
        }

        if (blue > red && blue > green) {
            // Likely water or river: lower cost for traversal (or higher if you consider it difficult)
            return new ClassificationResult(1.0, "water");
        } else if (green > red && green > blue) {
            // Forest/trees
            return new ClassificationResult(2.0, "forest");
        } else if (red > blue && green > blue) {
            // Land (brownish tone): higher cost
            return new ClassificationResult(3.0, "land");
        }
        // Default classification (could be refined)
        return new ClassificationResult(1.0, "unknown");
    }
}

