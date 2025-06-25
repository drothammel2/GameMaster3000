package games.BomberMan;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class BomberMan {
    public static void start(Runnable onExitToMenu) {
        System.out.println("BomberMan gestartet!");
        JFrame frame = new JFrame("BomberMan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set fullscreen mode
        frame.setUndecorated(true); // Remove window borders

        GameBoard gameBoard = new GameBoard();
        gameBoard.setQuitAction(() -> {
            frame.dispose(); // Close the game window
            onExitToMenu.run(); // Return to the main menu
        });

        frame.setLayout(new BorderLayout()); // Use BorderLayout to scale dynamically
        frame.add(gameBoard, BorderLayout.CENTER); // Add GameBoard to the center

        frame.setVisible(true);
    }
}
