import java.awt.Graphics2D;

public class Player {
    private double x;
    private double y;
    private final int size;
    private final double speed;
    private int health;
    private int maxHealth;
    private Weapon weapon;
    private long lastShotAt;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = 28;
        this.speed = 5.0;
        this.maxHealth = 100;
        this.health = maxHealth;
        this.weapon = new Weapon("Scout Blaster", 12, 180, 8.0, 4);
        this.lastShotAt = 0;
    }

    public void move(int dx, int dy, int width, int height) {
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            return;
        }

        x += (dx / length) * speed;
        y += (dy / length) * speed;

        x = Math.max(size, Math.min(width - size, x));
        y = Math.max(size, Math.min(height - size, y));
    }

    public boolean canShoot(long nowMs) {
        return nowMs - lastShotAt >= weapon.getCooldownMs();
    }

    public Projectile shoot(double targetX, double targetY, long nowMs) {
        lastShotAt = nowMs;
        double dx = targetX - x;
        double dy = targetY - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            length = 1;
        }

        return new Projectile(
            x,
            y,
            (dx / length) * weapon.getProjectileSpeed(),
            (dy / length) * weapon.getProjectileSpeed(),
            weapon.getDamage(),
            weapon.getProjectileRadius()
        );
    }

    public void draw(Graphics2D g2) {
        GraphicsEngine.drawPlayer(g2, (int) x, (int) y, size);
    }

    public void heal(int value) {
        health = Math.min(maxHealth, health + value);
    }

    public void damage(int value) {
        health = Math.max(0, health - value);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSize() {
        return size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void upgradeWeapon(Weapon upgradedWeapon) {
        this.weapon = upgradedWeapon;
    }
}
