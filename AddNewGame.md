# Neues Spiel hinzufügen

1. **Neues Package und Klasse anlegen**
   - Lege im Ordner `src/games/` einen neuen Unterordner für dein Spiel an, z.B. `Tetris`, `Dummy1`, `Dummy2`.
   - Erstelle darin eine Klasse, z.B. `Tetris.java`, `Dummy1.java`, `Dummy2.java` mit einer statischen Methode `start()`.

2. **Spiel-Klasse in Main.java importieren**
   - Füge in `Main.java` oben den Import hinzu:
     ```java
     import games.Tetris.Tetris;
     import games.Dummy1.Dummy1;
     import games.Dummy2.Dummy2;
     ```

3. **Switch-Statement in Main.java erweitern**
   - Ergänze einen neuen Fall:
     ```java
     case "Tetris":
         Tetris.start();
         break;
     case "Dummy1":
         Dummy1.start();
         break;
     case "Dummy2":
         Dummy2.start();
         break;
     ```

4. **Spielnamen in Window.java hinzufügen**
   - Ergänze in `Window.java` die Liste der Spiele:
     ```java
     startMainscreen(listener, Arrays.asList("Mario", "Snake", "Tetris", "Dummy1", "Dummy2"));
     ```

5. **Fertig!**
   - Die neuen Spiele erscheinen jetzt im Hauptmenü und können gestartet werden.

