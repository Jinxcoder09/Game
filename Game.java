import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Game {

    static int food = 5;             
    static int resources = 3;        
    static int daysSurvived = 0;     
    static final int MAX_DAYS = 10;  
    static final Random rand = new Random();

    static List<Animal> animals = List.of(
        new Animal("Deer", 4, 1), 
        new Animal("Rabbit", 2, 0), 
        new Animal("Bear", 6, 3)
    );

    static List<Weapon> weapons = List.of(
        new Weapon("Spear", 2), 
        new Weapon("Bow", 3), 
        new Weapon("Trap", 1)
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Jungle Mr.Hunt.....!");

        // Game loop
        while (daysSurvived < MAX_DAYS) {
            System.out.printf("Day %d | Food: %d, Resources: %d\n", daysSurvived + 1, food, resources);
            String weather = getWeather();
            System.out.println("Weather: " + weather);

            // Player action
            System.out.println("Choose action: (1) Hunt (2) Gather (3) Explore (4) Craft");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> hunt(weather);
                case 2 -> gather(weather);
                case 3 -> explore();
                case 4 -> craft();
                default -> System.out.println("Invalid choice. Select 1, 2, 3, or 4.");
            }

            dailyUpdate();
            if (food <= 0) {
                System.out.println("You ran out of food and couldn't survive. Game over!");
                break;
            }
        }

        if (daysSurvived == MAX_DAYS) {
            System.out.println("Congratulations! You survived for " + MAX_DAYS + " days!");
        }

        scanner.close();
    }

    private static String getWeather() {
        String[] weathers = {"Sunny", "Rainy", "Stormy", "Windy", "Blizzard"};
        return weathers[rand.nextInt(weathers.length)];
    }

    private static void hunt(String weather) {
        if (food <= 0) {
            System.out.println("You need food to hunt!");
            return;
        }
        food--; // Hunting costs 1 food

        // Choose a weapon
        System.out.println("Choose your weapon: ");
        for (int i = 0; i < weapons.size(); i++) {
            System.out.printf("%d. %s (Effectiveness: %d)\n", i + 1, weapons.get(i).name, weapons.get(i).effectiveness);
        }
        int weaponChoice = new Scanner(System.in).nextInt() - 1;
        Weapon selectedWeapon = weapons.get(weaponChoice);

        // Choose an animal to hunt
        Animal chosenAnimal = animals.get(rand.nextInt(animals.size()));
        System.out.printf("You are hunting a %s!\n", chosenAnimal.name);

        // Determine success
        int chanceToFind = selectedWeapon.effectiveness - chosenAnimal.difficulty + rand.nextInt(5);
        if (chanceToFind > 3) {
            food += chosenAnimal.foodValue;
            System.out.printf("You successfully hunted the %s and found %d food!\n", chosenAnimal.name, chosenAnimal.foodValue);
        } else {
            System.out.println("You failed to hunt the animal.");
        }
    }

    private static void gather(String weather) {
        // Gathering code remains unchanged
    }

    private static void explore() {
        // Exploring code remains unchanged
    }

    private static void craft() {
        // Crafting code remains unchanged
    }

    private static void dailyUpdate() {
        daysSurvived++;
        food--; // Lose 1 food per day
        System.out.println("End of day updates: You now have " + food + " food.");
    }

    static class Animal {
        String name;
        int foodValue;
        int difficulty;

        Animal(String name, int foodValue, int difficulty) {
            this.name = name;
            this.foodValue = foodValue;
            this.difficulty = difficulty;
        }
    }

    static class Weapon {
        String name;
        int effectiveness;

        Weapon(String name, int effectiveness) {
            this.name = name;
            this.effectiveness = effectiveness;
        }
    }
}
