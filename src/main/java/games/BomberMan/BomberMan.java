package games.BomberMan;

import javax.swing.JFrame;

public class BomberMan {
    public static void start() {
        System.out.println("BomberMan gestartet!");
        JFrame frame = new JFrame("BomberMan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new GameBoard());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setVisible(true);
    }
}
