public class Level {
    private int levelNumber;
    private String difficulty;
    private String[] levelMechanics;

    public Level(int levelNumber, String difficulty, String[] levelMechanics) {
        this.levelNumber = levelNumber;
        this.difficulty = difficulty;
        this.levelMechanics = levelMechanics;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String[] getLevelMechanics() {
        return levelMechanics;
    }

    public void displayLevelInfo() {
        System.out.println("Level: " + levelNumber + " | Difficulty: " + difficulty);
        System.out.print("Mechanics: ");
        for (String mechanic : levelMechanics) {
            System.out.print(mechanic + " ");
        }
        System.out.println();
    }
}