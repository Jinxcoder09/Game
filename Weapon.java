public class Weapon {
    private final String name;
    private final int damage;
    private final int cooldownMs;
    private final double projectileSpeed;
    private final int projectileRadius;

    public Weapon(String name, int damage, int cooldownMs, double projectileSpeed, int projectileRadius) {
        this.name = name;
        this.damage = damage;
        this.cooldownMs = cooldownMs;
        this.projectileSpeed = projectileSpeed;
        this.projectileRadius = projectileRadius;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getCooldownMs() {
        return cooldownMs;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    public int getProjectileRadius() {
        return projectileRadius;
    }
}
