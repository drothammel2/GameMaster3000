import javax.swing.plaf.basic.BasicBorders.MarginBorder;

public class Main {
    public static void main(String[] args) {

        Window.startMainscreen(new Window.GameSelectionListener() {
        
            @Override
            public void onGameSelected(String chosenGame) {
                switch(chosenGame) {
                    case "Mario":
                        Mario.start();
                        break;
                    case "Snake":
                        Snake.start();
                        break;
                    default:
                        System.out.println("Kein gültiges Spiel ausgewählt.");
                }
            }
        });
    }
}
