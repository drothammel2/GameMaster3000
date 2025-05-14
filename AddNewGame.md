# Neues Spiel hinzufügen

1. **Neues Package und Klasse anlegen**
   - Lege im Ordner `src/games/` einen neuen Unterordner für dein Spiel an, z.B. `Tetris`.
   - Erstelle darin eine Klasse `Tetris.java` mit einer statischen Methode `start()`.

2. **Spiel-Klasse in Main.java importieren**
   - Füge in `Main.java` oben den Import hinzu:
     ```java
     import games.Tetris.Tetris;
     ```

3. **Switch-Statement in Main.java erweitern**
   - Ergänze einen neuen Fall:
     ```java
     case "Tetris":
         Tetris.start();
         break;
     ```

4. **Spielnamen in Window.java hinzufügen**
   - Ergänze in `Window.java` die Liste der Spiele:
     ```java
     startMainscreen(listener, Arrays.asList("Mario", "Snake", "Tetris"));
     ```

5. **Fertig!**
   - Das neue Spiel erscheint jetzt im Hauptmenü und kann gestartet werden.

