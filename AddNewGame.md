# Neues Spiel hinzufügen

1. **Neues Package und Klasse anlegen**
   - Lege im Ordner `src/main/java/games/` einen neuen Unterordner für dein Spiel an, z.B. `Tetris`, `Dummy1`, `Dummy2`, `MonkeyType`.
   - Erstelle darin eine Klasse, z.B. `Tetris.java`, `Dummy1.java`, `Dummy2.java`, `MonkeyType.java` mit einer statischen Methode `start()`.

2. **Feature: Zurück zum Hauptmenü (Main Screen)**
   - Damit dein Spiel das Hauptmenü gezielt wieder anzeigen kann (z.B. über einen "Zurück zum Hauptmenü"-Button oder im Pause-Menü), passe die Signatur deiner `start`-Methode an:
     ```java
     public static void start(Runnable onExitToMenu)
     ```
   - Rufe die Methode `onExitToMenu.run();` immer dann auf, wenn du zum Hauptmenü zurückkehren möchtest (z.B. beim Klick auf einen Button oder im Game-Over-Dialog). Beispiel:
     ```java
     JButton backButton = new JButton("Zurück zum Hauptmenü");
     backButton.addActionListener(e -> {
         frame.dispose(); // Spiel-Fenster schließen
         onExitToMenu.run(); // Hauptmenü anzeigen
     });
     ```
   - **Hinweis:** Das Hauptmenü wird nur angezeigt, wenn du explizit `onExitToMenu.run();` aufrufst. Das kann z.B. im Pause-Menü, bei Game Over oder über einen eigenen Button passieren – du entscheidest wann!

3. **Spiel-Klasse in Main.java importieren**
   - Füge in `src/main/java/Main.java` oben den Import hinzu:
     ```java
     import games.Tetris.Tetris;
     import games.Dummy1.Dummy1;
     import games.Dummy2.Dummy2;
     import games.MonkeyType.MonkeyType;
     ```

4. **Switch-Statement in Main.java erweitern**
   - Ergänze einen neuen Fall und übergib die Rückkehr-Logik:
     ```java
     case "Tetris":
         Tetris.start(() -> Window.startMainscreen(this));
         break;
     ```
   - Für andere Spiele entsprechend:
     ```java
     case "Dummy1":
         Dummy1.start(() -> Window.startMainscreen(this));
         break;
     ```

5. **Spielnamen in Window.java hinzufügen**
   - Ergänze in `src/main/java/Window.java` die Liste der Spiele:
     ```java
     startMainscreen(listener, Arrays.asList("Mario", "Snake", "Tetris", "Dummy1", "Dummy2", "MonkeyType"));
     ```

6. **Fertig!**
   - Die neuen Spiele erscheinen jetzt im Hauptmenü und können gestartet werden. Das Hauptmenü kann gezielt aus jedem Spiel heraus wieder angezeigt werden, wenn du es möchtest.

