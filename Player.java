public class Player {
    private int positionX;
    private int positionY;
    private int health;
    private int food;
    private int resources;
    private String animation;

    public Player(int positionX, int positionY, int health, int food, int resources, String animation) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.health = health;
        this.food = food;
        this.resources = resources;
        this.animation = animation;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getHealth() {
        return health;
    }

    public int getFood() {
        return food;
    }

    public int getResources() {
        return resources;
    }

    public String getAnimation() {
        return animation;
    }

    public void setPosition(int x, int y) {
        this.positionX = x;
        this.positionY = y;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
    }
}