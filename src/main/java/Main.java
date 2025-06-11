import games.Dummy1.Dummy1;
import games.Dummy2.Dummy2;
import games.Mario.Mario;
import games.MonkeyType.MonkeyType;
import games.Snake.Snake;
import games.Speedrun.Speedrun;
import games.Tetris.Tetris;

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
                    case "Tetris":
                        Tetris.start();
                        break;
                    case "Dummy1":
                        Dummy1.start();
                        break;
                    case "Dummy2":
                        Dummy2.start();
                        break;
                    case "MonkeyType":
                        MonkeyType.start();
                        break;
                    case "Speedrun":
                        Speedrun.start();
                        break;
                    case "Quit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Kein gültiges Spiel ausgewählt.");
                }
            }
        });
    }
}