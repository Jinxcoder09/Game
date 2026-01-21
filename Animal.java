public class Animal {
    private String name;
    private int health;
    private int x, y; // Position coordinates

    public Animal(String name, int initialHealth) {
        this.name = name;
        this.health = initialHealth;
        this.x = 0;
        this.y = 0;
    }

    // Method for rendering graphics
    public void render() {
        // Code for rendering animal on screen will go here
        System.out.println("Rendering " + name + " at coordinates: (" + x + ", " + y + ")");
    }

    // Method for moving the animal
    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
        System.out.println(name + " moved to: (" + x + ", " + y + ")");
    }

    // Method to receive damage
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
        System.out.println(name + " took damage, health now: " + health);
    }

    // Getters for health and position
    public int getHealth() {
        return health;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}