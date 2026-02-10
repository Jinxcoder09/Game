import java.awt.Color;

public class Level {
    private final int levelNumber;
    private final String name;
    private final int enemyCount;
    private final double enemySpeed;
    private final int enemyHealth;
    private final int spawnIntervalTicks;
    private final Color skyTint;

    public Level(int levelNumber, String name, int enemyCount, double enemySpeed, int enemyHealth, int spawnIntervalTicks, Color skyTint) {
        this.levelNumber = levelNumber;
        this.name = name;
        this.enemyCount = enemyCount;
        this.enemySpeed = enemySpeed;
        this.enemyHealth = enemyHealth;
        this.spawnIntervalTicks = spawnIntervalTicks;
        this.skyTint = skyTint;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getName() {
        return name;
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public double getEnemySpeed() {
        return enemySpeed;
    }

    public int getEnemyHealth() {
        return enemyHealth;
    }

    public int getSpawnIntervalTicks() {
        return spawnIntervalTicks;
    }

    public Color getSkyTint() {
        return skyTint;
    }
}
