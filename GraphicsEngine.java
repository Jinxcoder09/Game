import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class GraphicsEngine {

    private GraphicsEngine() {}

    public static void drawBackground(Graphics2D g2, Level level, int width, int height, int tick) {
        Color tint = level.getSkyTint();
        g2.setColor(new Color(Math.max(0, tint.getRed() - 25), Math.max(0, tint.getGreen() - 25), Math.max(0, tint.getBlue() - 25)));
        g2.fillRect(0, 0, width, height);

        g2.setColor(tint);
        g2.fillRect(0, 0, width, (int) (height * 0.65));

        g2.setColor(new Color(34, 120, 52));
        g2.fillRect(0, (int) (height * 0.65), width, (int) (height * 0.35));

        g2.setColor(new Color(255, 255, 255, 90));
        int wave = (tick / 3) % width;
        g2.fillOval(-120 + wave, 40, 180, 60);
        g2.fillOval(180 + wave / 2, 85, 210, 70);
    }

    public static void drawPlayer(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(37, 42, 52));
        g2.fillOval(x - size / 2, y - size / 2, size, size);
        g2.setColor(new Color(83, 188, 255));
        g2.fillOval(x - size / 3, y - size / 3, size / 2, size / 2);
    }

    public static void drawEnemy(Graphics2D g2, String type, int x, int y, int size, int health) {
        Color enemyColor = switch (type) {
            case "Tiger" -> new Color(231, 136, 45);
            case "Wolf" -> new Color(144, 145, 158);
            default -> new Color(120, 65, 45);
        };

        g2.setColor(enemyColor);
        g2.fillOval(x - size / 2, y - size / 2, size, size);

        g2.setColor(Color.BLACK);
        g2.fillOval(x - 4, y - 2, 3, 3);
        g2.fillOval(x + 1, y - 2, 3, 3);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRect(x - size / 2, y - size / 2 - 8, size, 5);
        g2.setColor(new Color(235, 70, 70));
        g2.fillRect(x - size / 2, y - size / 2 - 8, Math.max(0, Math.min(size, health * size / 60)), 5);
    }

    public static void drawProjectile(Graphics2D g2, Projectile projectile) {
        g2.setColor(new Color(255, 239, 90));
        int radius = projectile.radius();
        g2.fillOval((int) projectile.x() - radius, (int) projectile.y() - radius, radius * 2, radius * 2);
    }

    public static void drawHud(Graphics2D g2, Player player, Level level, int score, int enemiesLeft) {
        g2.setColor(new Color(0, 0, 0, 125));
        g2.fillRoundRect(12, 12, 340, 85, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("LEVEL " + level.getLevelNumber() + " - " + level.getName(), 24, 34);
        g2.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 24, 54);
        g2.drawString("Weapon: " + player.getWeapon().getName(), 24, 74);
        g2.drawString("Score: " + score + "  Enemies Left: " + enemiesLeft, 170, 54);
    }

    public static void drawCenterMessage(Graphics2D g2, String line1, String line2, int width, int height) {
        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRoundRect(width / 2 - 260, height / 2 - 70, 520, 140, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        int l1W = g2.getFontMetrics().stringWidth(line1);
        g2.drawString(line1, width / 2 - l1W / 2, height / 2 - 10);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        int l2W = g2.getFontMetrics().stringWidth(line2);
        g2.drawString(line2, width / 2 - l2W / 2, height / 2 + 24);
    }

    public static void drawCrosshair(Graphics2D g2, int mouseX, int mouseY) {
        g2.setColor(new Color(255, 255, 255, 190));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(mouseX - 12, mouseY - 12, 24, 24);
        g2.drawLine(mouseX - 18, mouseY, mouseX + 18, mouseY);
        g2.drawLine(mouseX, mouseY - 18, mouseX, mouseY + 18);
    }
}
