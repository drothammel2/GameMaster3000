import games.Dummy1.Dummy1;
import games.Dummy2.Dummy2;
import games.Mario.Mario;
import games.MonkeyType.MonkeyType;
import games.Speedrun.Speedrun;
import games.Tetris.Tetris;
import games.BomberMan.BomberMan;
import games.Snake.Snake;

public class Main {
    public static void main(String[] args) {
        
        Window.startMainscreen(new Window.GameSelectionListener() {
        
            @Override
            public void onGameSelected(String chosenGame) {
                switch(chosenGame) {
                    case "Mario":
                        Mario.start();
                        break;
                    case "MonkeyType":
                        MonkeyType.start();
                        break;    
                    case "Snake":
                        Snake.start(() -> Window.startMainscreen(this));
                        break;
                    case "Speedrun":
                        Speedrun.start(() -> Window.startMainscreen(this));
                        break;
                    case "Quit":
                        System.exit(0);
                        break;
                    case "BomberMan":
                        BomberMan.start();
                        break;
                    case "Tetris":
                        Tetris.start(() -> Window.startMainscreen(this));
                        break;
                    default:
                        System.out.println("Kein gültiges Spiel ausgewählt.");
                }
            }
        });
    }
}