package application;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RealisticMapGenerator {

    private Canvas canvas;
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final Random random = new Random();

    public VBox getMapView() {
        canvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);
        drawRealisticTerrain();

        Button saveButton = new Button("Save Map");
        saveButton.setOnAction(e -> saveMapImage());

        VBox root = new VBox(canvas, saveButton);
        return root;
    }

    private void drawRealisticTerrain() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFillRule(FillRule.EVEN_ODD);
        
        generateTerrainLayer(gc, 0.015, 
            Color.SADDLEBROWN, Color.PERU, Color.DARKOLIVEGREEN);
        
        drawRiver(gc);
        drawLake(gc, 600, 400, 80);
        generateForest(gc, 200, 150, 120);
        generateForest(gc, 50, 300, 90);
        drawMountainRange(gc, 400, 200, 250, 150);
        drawWetland(gc, 150, 500, 100);
    }

    private void generateTerrainLayer(GraphicsContext gc, double noiseScale, 
                                     Color... colors) {
        gc.save();
        double[][] elevation = new double[MAP_WIDTH][MAP_HEIGHT];
        
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                elevation[x][y] = noise(x * noiseScale, y * noiseScale);
                double e = Math.max(0.0, Math.min(1.0, elevation[x][y]));
                
                if (e < 0.3) {
                    gc.setFill(getWaterColor(e));
                } else if (e < 0.5) {
                    gc.setFill(getWetlandColor(e));
                } else {
                    gc.setFill(getLandColor(e, colors));
                }
                gc.fillRect(x, y, 1, 1);
            }
        }
        gc.restore();
    }

    private Color getLandColor(double elevation, Color... colors) {
        double t = Math.max(0, Math.min(1, (elevation - 0.5) * 2));
        Stop[] stops = new Stop[colors.length];
        for (int i = 0; i < colors.length; i++) {
            stops[i] = new Stop((double)i/(colors.length-1), colors[i]);
        }
        
        for (int i = 1; i < stops.length; i++) {
            if (t <= stops[i].getOffset()) {
                double segmentStart = stops[i-1].getOffset();
                double segmentLength = stops[i].getOffset() - segmentStart;
                double blend = (t - segmentStart)/segmentLength;
                return stops[i-1].getColor()
                    .interpolate(stops[i].getColor(), blend);
            }
        }
        return stops[stops.length-1].getColor();
    }

    private Color getWaterColor(double depth) {
        depth = Math.max(0.0, Math.min(0.3, depth)); // Clamp to valid range
        double green = Math.max(0.0, Math.min(1.0, 0.3 + depth * 0.4));
        double blue = Math.max(0.0, Math.min(1.0, 0.5 + depth * 0.3));
        return Color.color(0.1, green, blue, 0.7);
    }

    private Color getWetlandColor(double moisture) {
        moisture = Math.max(0.3, Math.min(0.5, moisture)); // Clamp to valid range
        double red = Math.max(0.0, Math.min(1.0, 0.6));
        double green = Math.max(0.0, Math.min(1.0, 0.8 - moisture * 0.3));
        double blue = Math.max(0.0, Math.min(1.0, 0.3 + moisture * 0.1));
        return Color.color(red, green, blue);
    }

    private void drawRiver(GraphicsContext gc) {
        gc.save();
        gc.setStroke(new Color(0.2, 0.5, 0.8, 0.7));
        gc.setLineWidth(6 + random.nextDouble(8));
        gc.setLineCap(StrokeLineCap.ROUND);
        
        double x = 50;
        double y = 100 + random.nextDouble(300);
        gc.beginPath();
        gc.moveTo(x, y);
        
        while (x < MAP_WIDTH - 50) {
            x += 10 + random.nextDouble(20);
            y += (random.nextDouble(40) - 20);
            y = Math.max(50, Math.min(MAP_HEIGHT - 50, y));
            gc.lineTo(x, y);
        }
        gc.stroke();
        gc.restore();
    }

    private void generateForest(GraphicsContext gc, int x, int y, int radius) {
        gc.save();
        gc.setFill(createForestPattern());
        
        for (int i = 0; i < 200; i++) {
            double angle = random.nextDouble(Math.PI * 2);
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double px = x + Math.cos(angle) * dist;
            double py = y + Math.sin(angle) * dist;
            
            gc.fillOval(px, py, 2 + random.nextDouble(4), 
                2 + random.nextDouble(4));
        }
        gc.restore();
    }

    private Color createForestPattern() {
        return Color.color(
            0.1 + random.nextDouble(0.15),
            0.4 + random.nextDouble(0.3),
            0.1 + random.nextDouble(0.1)
        );
    }

    private void drawMountainRange(GraphicsContext gc, int x, int y, 
                                  int width, int height) {
        gc.save();
        gc.setFill(createMountainGradient());
        
        double[] peakX = new double[12];
        double[] peakY = new double[12];
        
        for (int i = 0; i < 12; i++) {
            peakX[i] = x + i * width/12 + random.nextDouble(15) - 7.5;
            peakY[i] = y + height - random.nextDouble(height * 0.8);
        }
        
        gc.fillPolygon(peakX, peakY, 12);
        addMountainDetails(gc, peakX, peakY);
        gc.restore();
    }

    private void addMountainDetails(GraphicsContext gc, double[] xPoints, 
                                   double[] yPoints) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        
        for (int i = 0; i < xPoints.length-1; i++) {
            double midX = (xPoints[i] + xPoints[i+1])/2;
            double midY = (yPoints[i] + yPoints[i+1])/2;
            gc.strokeLine(midX, midY, 
                midX + random.nextDouble(15) - 7.5, 
                midY - random.nextDouble(30));
        }
    }

    private LinearGradient createMountainGradient() {
        return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.DIMGREY),
            new Stop(0.6, Color.STEELBLUE),
            new Stop(1, Color.LIGHTGREY));
    }

    private void drawLake(GraphicsContext gc, int x, int y, int radius) {
        gc.save();
        gc.setFill(createLakeGradient(x, y, radius));
        gc.fillOval(x - radius/2, y - radius/2, radius, radius);
        addRipples(gc, x, y, radius);
        gc.restore();
    }

    private RadialGradient createLakeGradient(int x, int y, int radius) {
        return new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE,
            new Stop(0, new Color(0.1, 0.5, 0.8, 0.8)),
            new Stop(1, new Color(0.0, 0.3, 0.6, 0.6)));
    }

    private void addRipples(GraphicsContext gc, int x, int y, int radius) {
        gc.setStroke(new Color(0.2, 0.6, 0.9, 0.4));
        gc.setLineWidth(1);
        
        for (int i = 0; i < 3; i++) {
            gc.strokeOval(
                x - radius/2 - i*8, 
                y - radius/2 - i*8, 
                radius + i*16, 
                radius + i*16
            );
        }
    }

    private void drawWetland(GraphicsContext gc, int x, int y, int radius) {
        gc.save();
        gc.setFill(createWetlandPattern());
        
        for (int i = 0; i < 300; i++) {
            double angle = random.nextDouble(Math.PI * 2);
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double px = x + Math.cos(angle) * dist;
            double py = y + Math.sin(angle) * dist;
            
            gc.fillRect(px, py, 1, 1);
        }
        gc.restore();
    }

    private Color createWetlandPattern() {
        return Color.color(
            0.5 + random.nextDouble(0.15),
            0.7 + random.nextDouble(0.15),
            0.3 + random.nextDouble(0.1)
        );
    }

    private double noise(double x, double y) {
        int n = (int)x << 16 | (int)y;
        n = (n << 13) ^ n;
        int hash = n * (n * n * 15731 + 789221) + 1376312589;
        return (hash & 0x7fffffff) / (double)0x7fffffff; // 0.0 to 1.0
    }

    private void saveMapImage() {
        WritableImage image = canvas.snapshot(null, null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Wildlife Map");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), 
                    "png", file);
                System.out.println("Map saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving map: " + e.getMessage());
            }
        }
    }
}