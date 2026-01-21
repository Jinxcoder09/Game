// GraphicsEngine.java

import java.awt.*;
import javax.swing.*;

public class GraphicsEngine {
    private JFrame frame;
    private Canvas canvas;

    public GraphicsEngine() {
        setupFrame();
    }

    private void setupFrame() {
        frame = new JFrame("Graphics Engine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new Canvas();
        frame.add(canvas);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    public void render(Graphics g) {
        // Clear the screen
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Example: drawing a rectangle for the terrain
        g.setColor(Color.GREEN);
        g.fillRect(0, canvas.getHeight() - 50, canvas.getWidth(), 50);
        // Render additional elements here:
        // - Draw sprites
        // - Add animations
        // - Implement weather effects
    }

    public void drawSprite(Image sprite, int x, int y) {
        Graphics g = canvas.getGraphics();
        g.drawImage(sprite, x, y, null);
    }

    public void updateWeatherEffect(String effect) {
        // Implement weather effects logic here
        // For example: rain, snow, etc.
    }

    public void renderUI() {
        // Implement UI rendering logic here
        // For example: health bars, menus, etc.
    }

    public static void main(String[] args) {
        GraphicsEngine engine = new GraphicsEngine();
        engine.render(engine.canvas.getGraphics());
    }
}