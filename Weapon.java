public class Weapon {
    private String name;
    private int effectiveness;
    private int durability;

    public Weapon(String name, int effectiveness, int durability) {
        this.name = name;
        this.effectiveness = effectiveness;
        this.durability = durability;
    }

    public String getName() {
        return name;
    }

    public int getEffectiveness() {
        return effectiveness;
    }

    public int getDurability() {
        return durability;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEffectiveness(int effectiveness) {
        this.effectiveness = effectiveness;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }
}