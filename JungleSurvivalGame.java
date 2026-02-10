import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JungleSurvivalGame extends JFrame {

    public JungleSurvivalGame() {
        setTitle("Jungle Strike: Survivor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JungleSurvivalGame game = new JungleSurvivalGame();
            game.setVisible(true);
        });
    }
}
