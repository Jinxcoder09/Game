public record Projectile(double x, double y, double vx, double vy, int damage, int radius) {
    public Projectile step() {
        return new Projectile(x + vx, y + vy, vx, vy, damage, radius);
    }
}
