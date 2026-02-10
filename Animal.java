import java.awt.Graphics2D;

public class Animal {
    private final String type;
    private double x;
    private double y;
    private final int size;
    private final double speed;
    private int health;
    private final int contactDamage;

    public Animal(String type, double x, double y, int size, double speed, int health, int contactDamage) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.health = health;
        this.contactDamage = contactDamage;
    }

    public void update(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            return;
        }

        x += (dx / length) * speed;
        y += (dy / length) * speed;
    }

    public void draw(Graphics2D g2) {
        GraphicsEngine.drawEnemy(g2, type, (int) x, (int) y, size, health);
    }

    public boolean collidesWithPlayer(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        return Math.sqrt(dx * dx + dy * dy) <= player.getSize() * 0.5 + size * 0.5;
    }

    public boolean containsPoint(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) <= size * 0.5;
    }

    public void takeDamage(int value) {
        health -= value;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getContactDamage() {
        return contactDamage;
    }
}
