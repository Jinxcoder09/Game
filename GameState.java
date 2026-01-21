public class GameState {
    private PlayerStats playerStats;
    private Resources resources;
    private List<Animal> animals;
    private LevelProgression levelProgression;

    public GameState() {
        this.playerStats = new PlayerStats();
        this.resources = new Resources();
        this.animals = new ArrayList<>();
        this.levelProgression = new LevelProgression();
    }

    // Getters and Setters

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void addAnimal(Animal animal) {
        this.animals.add(animal);
    }

    public LevelProgression getLevelProgression() {
        return levelProgression;
    }

    public void setLevelProgression(LevelProgression levelProgression) {
        this.levelProgression = levelProgression;
    }
}