import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 640;

    private final Timer timer;
    private final Random random;
    private final SoundEngine soundEngine;
    private final List<Level> levels;

    private final List<Animal> enemies;
    private final List<Projectile> projectiles;
    private final boolean[] keys;

    private Player player;
    private GameState gameState;
    private int levelIndex;
    private int enemiesSpawned;
    private int score;
    private int tick;
    private int mouseX;
    private int mouseY;
    private int damageCooldown;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        random = new Random();
        soundEngine = new SoundEngine();
        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();
        keys = new boolean[256];

        levels = List.of(
            new Level(1, "Rainforest Outskirts", 16, 1.1, 30, 40, new java.awt.Color(76, 158, 92)),
            new Level(2, "Fog Basin", 22, 1.4, 45, 30, new java.awt.Color(80, 117, 145)),
            new Level(3, "Ruins of the Apex", 30, 1.9, 60, 22, new java.awt.Color(132, 96, 162))
        );

        setupInput();
        resetToMenu();

        timer = new Timer(16, this);
        timer.start();
    }

    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) {
                    keys[code] = true;
                }

                if (code == KeyEvent.VK_ENTER && gameState == GameState.MENU) {
                    startGame();
                } else if (code == KeyEvent.VK_R && (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY)) {
                    startGame();
                } else if (code == KeyEvent.VK_P && gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED;
                } else if (code == KeyEvent.VK_P && gameState == GameState.PAUSED) {
                    gameState = GameState.PLAYING;
                } else if (code == KeyEvent.VK_SPACE && gameState == GameState.LEVEL_COMPLETE) {
                    beginNextLevel();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) {
                    keys[code] = false;
                }
            }
        });

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                mouseX = e.getX();
                mouseY = e.getY();

                if (gameState == GameState.PLAYING && player.canShoot(System.currentTimeMillis())) {
                    projectiles.add(player.shoot(mouseX, mouseY, System.currentTimeMillis()));
                    soundEngine.playShoot();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void resetToMenu() {
        gameState = GameState.MENU;
        player = new Player(WIDTH / 2.0, HEIGHT / 2.0);
        enemies.clear();
        projectiles.clear();
        levelIndex = 0;
        enemiesSpawned = 0;
        score = 0;
        tick = 0;
        damageCooldown = 0;
    }

    private void startGame() {
        resetToMenu();
        gameState = GameState.PLAYING;
        beginCurrentLevel();
    }

    private void beginCurrentLevel() {
        enemies.clear();
        projectiles.clear();
        enemiesSpawned = 0;
    }

    private void beginNextLevel() {
        if (levelIndex >= levels.size() - 1) {
            gameState = GameState.VICTORY;
            soundEngine.playVictory();
            return;
        }

        levelIndex++;
        if (levelIndex == 1) {
            player.upgradeWeapon(new Weapon("Predator Rifle", 18, 140, 9.2, 4));
        } else if (levelIndex == 2) {
            player.upgradeWeapon(new Weapon("Apex Cannon", 24, 120, 10.4, 5));
        }
        player.heal(20);
        gameState = GameState.PLAYING;
        beginCurrentLevel();
        soundEngine.playLevelUp();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tick++;
        if (damageCooldown > 0) {
            damageCooldown--;
        }

        if (gameState == GameState.PLAYING) {
            updatePlayingState();
        }

        repaint();
    }

    private void updatePlayingState() {
        Level level = levels.get(levelIndex);
        handleMovement();

        if (enemiesSpawned < level.getEnemyCount() && tick % level.getSpawnIntervalTicks() == 0) {
            enemies.add(spawnEnemy(level));
            enemiesSpawned++;
        }

        for (Animal enemy : enemies) {
            enemy.update(player);
        }

        updateProjectiles();
        handleCollisions();

        if (player.getHealth() <= 0) {
            gameState = GameState.GAME_OVER;
            soundEngine.playGameOver();
        }

        if (enemiesSpawned >= level.getEnemyCount() && enemies.isEmpty()) {
            gameState = GameState.LEVEL_COMPLETE;
            soundEngine.playLevelUp();
        }
    }

    private void handleMovement() {
        int dx = 0;
        int dy = 0;
        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) dy -= 1;
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) dy += 1;
        if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) dx -= 1;
        if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) dx += 1;
        player.move(dx, dy, WIDTH, HEIGHT);
    }

    private Animal spawnEnemy(Level level) {
        int side = random.nextInt(4);
        int x;
        int y;
        switch (side) {
            case 0 -> { x = random.nextInt(WIDTH); y = 0; }
            case 1 -> { x = random.nextInt(WIDTH); y = HEIGHT; }
            case 2 -> { x = 0; y = random.nextInt(HEIGHT); }
            default -> { x = WIDTH; y = random.nextInt(HEIGHT); }
        }

        String type = level.getLevelNumber() == 1 ? "Wolf" : level.getLevelNumber() == 2 ? "Tiger" : "Alpha";
        int size = 26 + level.getLevelNumber() * 3;
        int damage = 8 + level.getLevelNumber() * 2;
        return new Animal(type, x, y, size, level.getEnemySpeed(), level.getEnemyHealth(), damage);
    }

    private void updateProjectiles() {
        for (int i = 0; i < projectiles.size(); i++) {
            projectiles.set(i, projectiles.get(i).step());
        }
        projectiles.removeIf(p -> p.x() < 0 || p.x() > WIDTH || p.y() < 0 || p.y() > HEIGHT);
    }

    private void handleCollisions() {
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            boolean hit = false;

            for (Animal enemy : enemies) {
                if (enemy.containsPoint(projectile.x(), projectile.y())) {
                    enemy.takeDamage(projectile.damage());
                    projectileIterator.remove();
                    hit = true;
                    soundEngine.playHit();
                    if (enemy.isDead()) {
                        score += 100;
                    }
                    break;
                }
            }

            if (hit) {
                enemies.removeIf(Animal::isDead);
            }
        }

        if (damageCooldown == 0) {
            for (Animal enemy : enemies) {
                if (enemy.collidesWithPlayer(player)) {
                    player.damage(enemy.getContactDamage());
                    damageCooldown = 25;
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Level currentLevel = levels.get(Math.min(levelIndex, levels.size() - 1));
        GraphicsEngine.drawBackground(g2, currentLevel, WIDTH, HEIGHT, tick);

        for (Animal enemy : enemies) {
            enemy.draw(g2);
        }

        for (Projectile projectile : projectiles) {
            GraphicsEngine.drawProjectile(g2, projectile);
        }

        player.draw(g2);
        GraphicsEngine.drawCrosshair(g2, mouseX, mouseY);
        int enemiesLeft = Math.max(0, currentLevel.getEnemyCount() - enemiesSpawned + enemies.size());
        GraphicsEngine.drawHud(g2, player, currentLevel, score, enemiesLeft);

        if (gameState == GameState.MENU) {
            GraphicsEngine.drawCenterMessage(g2, "JUNGLE STRIKE: SURVIVOR", "Press ENTER to start. Move: WASD / Arrows, Shoot: Mouse", WIDTH, HEIGHT);
        } else if (gameState == GameState.PAUSED) {
            GraphicsEngine.drawCenterMessage(g2, "PAUSED", "Press P to continue.", WIDTH, HEIGHT);
        } else if (gameState == GameState.LEVEL_COMPLETE) {
            GraphicsEngine.drawCenterMessage(g2, "LEVEL CLEARED", "Press SPACE for next level.", WIDTH, HEIGHT);
        } else if (gameState == GameState.GAME_OVER) {
            GraphicsEngine.drawCenterMessage(g2, "GAME OVER", "Press R to restart.", WIDTH, HEIGHT);
        } else if (gameState == GameState.VICTORY) {
            GraphicsEngine.drawCenterMessage(g2, "VICTORY", "You conquered all jungle sectors! Press R to replay.", WIDTH, HEIGHT);
        }
    }
}
