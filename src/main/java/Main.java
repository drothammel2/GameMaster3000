import games.BomberMan.BomberMan;
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
                        Snake.start(() -> Window.startMainscreen(this));
                        break;
                    case "MonkeyType":
                        MonkeyType.start(() -> Window.startMainscreen(this));
                        break;
                    case "Speedrun":
                        Speedrun.start(() -> Window.startMainscreen(this));
                        break;
                    case "Quit":
                        System.exit(0);
                        break;
                    case "BomberMan":
                        BomberMan.start(() -> Window.startMainscreen(this));
                        break;
                    case "Tetris":
                        Tetris.start();
                        break;
                    default:
                        System.out.println("Kein gültiges Spiel ausgewählt.");
                }
            }
        });
    }
}