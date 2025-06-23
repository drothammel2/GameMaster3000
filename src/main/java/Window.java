import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.util.List;
import java.util.Arrays;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.awt.Cursor;
import players.Players;

public class Window {

    public interface GameSelectionListener {
        void onGameSelected(String gameName);
    }

    public static void startMainscreen(GameSelectionListener listener, List<String> gameNames) {
        // Spieler-Auswahl vor dem Hauptmenü ENTFERNT!
        showGameMenu(listener, gameNames);
    }

    private static void showGameMenu(GameSelectionListener listener, List<String> gameNames) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GameMaster3000");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            // Hauptpanel mit schwarzem Hintergrund
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.BLACK);

            // Gemeinsames Panel für Spieleranzeige und Überschrift oben
            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new javax.swing.BoxLayout(titlePanel, javax.swing.BoxLayout.Y_AXIS));
            titlePanel.setBackground(Color.BLACK);
            String currentPlayer = (Players.getCurrentPlayer() != null && Players.getCurrentPlayer().name != null && !Players.getCurrentPlayer().name.isEmpty())
                ? Players.getCurrentPlayer().name : "Wähle einen Spieler..!";
            JLabel playerInfoLabel = new JLabel(currentPlayer, JLabel.CENTER);
            playerInfoLabel.setFont(new Font("Arial", Font.BOLD, 48));
            playerInfoLabel.setForeground(Color.ORANGE);
            playerInfoLabel.setAlignmentX(0.5f);
            titlePanel.add(playerInfoLabel);
            JLabel label = new JLabel("und wähle ein Spiel:", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 36));
            label.setForeground(Color.WHITE);
            label.setAlignmentX(0.5f);
            titlePanel.add(label);
            mainPanel.add(titlePanel, BorderLayout.NORTH);

            // Center-Panel für den animierten Spiele-Button und Navigation
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            // Animation Panel für die Spiele-Knöpfe (Null-Layout)
            JPanel animationPanel = new JPanel(null);
            animationPanel.setOpaque(false);
            centerPanel.add(animationPanel, BorderLayout.CENTER);

            // --- Gemeinsames Panel für Player- und Quit-Button unten zentriert ---
            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            southPanel.setOpaque(false);
            JButton playerButton = new RoundedButton("Player");
            playerButton.setFont(new Font("Arial", Font.BOLD, 36));
            playerButton.setBackground(Color.BLACK);
            playerButton.setForeground(Color.WHITE);
            playerButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 3));
            playerButton.addActionListener(e -> {
                Players.showPlayerDialog(frame);
                // Update label after dialog closes
                String updatedPlayer = (Players.getCurrentPlayer() != null && Players.getCurrentPlayer().name != null && !Players.getCurrentPlayer().name.isEmpty())
                    ? Players.getCurrentPlayer().name : "Wähle einen Spieler..!";
                playerInfoLabel.setText(updatedPlayer);
            });
            southPanel.add(playerButton);
            JButton quitButton = new RoundedButton("Quit");
            quitButton.setFont(new Font("Arial", Font.BOLD, 36));
            quitButton.addActionListener(e -> {
                frame.dispose();
                listener.onGameSelected("Quit");
            });
            southPanel.add(quitButton);
            mainPanel.add(southPanel, BorderLayout.SOUTH);

            // currentIndex als veränderliche Variable (über ein Array)
            final int[] currentIndex = {0};

            // Flag, ob gerade animiert wird
            final boolean[] isAnimating = {false};

            // Erzeuge initial drei Buttons: links, Mitte, rechts
            int size = gameNames.size();
            int leftIndex = (currentIndex[0] - 1 + size) % size;
            int rightIndex = (currentIndex[0] + 1) % size;

            JButton leftGameButton = createGameButton(gameNames.get(leftIndex));
            leftGameButton.addActionListener(e -> {
                frame.dispose();
                listener.onGameSelected(gameNames.get(leftIndex));
            });
            JButton centerGameButton = createGameButton(gameNames.get(currentIndex[0]));
            centerGameButton.addActionListener(e -> {
                frame.dispose();
                listener.onGameSelected(gameNames.get(currentIndex[0]));
            });
            JButton rightGameButton = createGameButton(gameNames.get(rightIndex));
            rightGameButton.addActionListener(e -> {
                frame.dispose();
                listener.onGameSelected(gameNames.get(rightIndex));
            });

            // Füge die drei Buttons zum Animation Panel hinzu
            animationPanel.add(leftGameButton);
            animationPanel.add(centerGameButton);
            animationPanel.add(rightGameButton);
            final JButton[] currentButtons = { leftGameButton, centerGameButton, rightGameButton };

            // Setze den Fokus initial auf das mittlere Element
            centerGameButton.requestFocusInWindow();

            // Zentrierung und Positionierung der drei Buttons bei Größenänderung
            animationPanel.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent evt) {
                    int panelWidth = animationPanel.getWidth();
                    int panelHeight = animationPanel.getHeight();
                    final int spacing = 20; // Abstand zwischen den Buttons
                    // Berechne die Knopfgröße: Maximale Breite 300px, verfügbare Breite abzüglich Abstände
                    int availableWidth = panelWidth - 2 * spacing;
                    int btnWidth = Math.min(300, availableWidth / 3);
                    // Erhöhe die Höhe: zum Beispiel 80% der Panelhöhe, maximal 250px
                    int btnHeight = Math.min(250, (int)(panelHeight * 0.8));
                    int startX = (panelWidth - 3 * btnWidth - 2 * spacing) / 2;
                    int y = (panelHeight - btnHeight) / 2;
                    currentButtons[0].setBounds(startX, y, btnWidth, btnHeight);
                    currentButtons[1].setBounds(startX + btnWidth + spacing, y, btnWidth, btnHeight);
                    currentButtons[2].setBounds(startX + 2 * (btnWidth + spacing), y, btnWidth, btnHeight);
                }
            });

            // Linker Pfeil-Button
            JButton leftButton = new RoundedButton("<");
            leftButton.setFont(new Font("Arial", Font.BOLD, 36));
            leftButton.setContentAreaFilled(false);
            leftButton.setOpaque(true);
            leftButton.setBackground(Color.BLACK);
            leftButton.setForeground(Color.WHITE);
            leftButton.setBorderPainted(false);  // Rahmen komplett deaktivieren
            leftButton.setPreferredSize(new java.awt.Dimension(100, leftButton.getPreferredSize().height));
            leftButton.addActionListener(e -> {
                if(isAnimating[0]) return;
                animateTripleTransition(frame, animationPanel, currentButtons, -1, listener, gameNames, currentIndex, isAnimating);
            });
            centerPanel.add(leftButton, BorderLayout.WEST);

            // Rechter Pfeil-Button
            JButton rightButton = new RoundedButton(">");
            rightButton.setFont(new Font("Arial", Font.BOLD, 36));
            rightButton.setContentAreaFilled(false);
            rightButton.setOpaque(true);
            rightButton.setBackground(Color.BLACK);
            rightButton.setForeground(Color.WHITE);
            rightButton.setBorderPainted(false);  // Rahmen komplett deaktivieren
            rightButton.setPreferredSize(new java.awt.Dimension(100, rightButton.getPreferredSize().height));
            rightButton.addActionListener(e -> {
                if(isAnimating[0]) return;
                animateTripleTransition(frame, animationPanel, currentButtons, 1, listener, gameNames, currentIndex, isAnimating);
            });
            centerPanel.add(rightButton, BorderLayout.EAST);

            frame.add(mainPanel);

            // Tastatur-Action für Pfeiltasten und A/D
            frame.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke("LEFT"), "leftAction");
            frame.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke('A', 0), "leftAction");
            frame.getRootPane().getActionMap().put("leftAction", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if(isAnimating[0]) return;
                    animateTripleTransition(frame, animationPanel, currentButtons, -1, listener, gameNames, currentIndex, isAnimating);
                }
            });

            frame.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke("RIGHT"), "rightAction");
            frame.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke('D', 0), "rightAction");
            frame.getRootPane().getActionMap().put("rightAction", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if(isAnimating[0]) return;
                    animateTripleTransition(frame, animationPanel, currentButtons, 1, listener, gameNames, currentIndex, isAnimating);
                }
            });

            frame.setVisible(true);
        });
    }

    // Hilfsmethode zur Erstellung eines Spiele-Buttons (unverändert)
    private static JButton createGameButton(String text) {
        JButton button = new RoundedButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 36));
        button.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 3));
        return button;
    }

    // Neue Methode zur Animation der Verschiebung der drei Buttons
    // direction: 1 -> Verschiebung nach links (nächstes Spiel -> Rechts kommt neu),
    //            -1 -> Verschiebung nach rechts (vorheriges Spiel -> Links kommt neu)
    private static void animateTripleTransition(JFrame frame, JPanel animationPanel, JButton[] currentButtons,
                                                int direction, GameSelectionListener listener, List<String> gameNames,
                                                int[] currentIndex, boolean[] isAnimating) {
        if(isAnimating[0]) return;
        isAnimating[0] = true;
        int btnWidth = currentButtons[1].getWidth();
        int btnHeight = currentButtons[1].getHeight();
        final int spacing = 20; // muss zum ComponentListener passen
        
        int steps = 10; // weniger Schritte für schnellere Animation
        int animationDelay = 10; // ms pro Schritt
        int totalShift = btnWidth + spacing;
        int shift = totalShift / steps;
        int moveDelta = (direction == 1) ? -shift : shift;
        
        int size = gameNames.size();
        final JButton[] newButtonArr = new JButton[1];
        if(direction == 1) {
            // Rechts-Pfeil: Neues Triple: rechts = neues Spiel
            int newRightIndex = (currentIndex[0] + 2) % size;
            JButton newButton = createGameButton(gameNames.get(newRightIndex));
            newButton.addActionListener(e -> {
                frame.dispose();
                listener.onGameSelected(gameNames.get(newRightIndex));
            });
            // Positioniere neuen Button rechts neben dem aktuellen rechten Button 
            int x = currentButtons[2].getX() + btnWidth + spacing;
            int y = currentButtons[2].getY();
            newButton.setBounds(x, y, btnWidth, btnHeight);
            animationPanel.add(newButton);
            animationPanel.setComponentZOrder(newButton, 0);
            newButtonArr[0] = newButton;
        } else {
            // Links-Pfeil: Neues Triple: links = neues Spiel
            int newLeftIndex = (currentIndex[0] - 2 + size) % size;
            JButton newButton = createGameButton(gameNames.get(newLeftIndex));
            newButton.addActionListener(e -> {
                frame.dispose();
                listener.onGameSelected(gameNames.get(newLeftIndex));
            });
            // Positioniere neuen Button links neben dem aktuellen linken Button
            int x = currentButtons[0].getX() - btnWidth - spacing;
            int y = currentButtons[0].getY();
            newButton.setBounds(x, y, btnWidth, btnHeight);
            animationPanel.add(newButton);
            animationPanel.setComponentZOrder(newButton, 0);
            newButtonArr[0] = newButton;
        }
        
        Timer timer = new Timer(animationDelay, null);
        timer.addActionListener(new ActionListener() {
            int count = 0;
            public void actionPerformed(ActionEvent e) {
                count++;
                // Verschiebe alle Buttons um moveDelta
                for(JButton btn : currentButtons) {
                    btn.setLocation(btn.getX() + moveDelta, btn.getY());
                }
                newButtonArr[0].setLocation(newButtonArr[0].getX() + moveDelta, newButtonArr[0].getY());
                if (count >= steps) {
                    timer.stop();
                    if (direction == 1) {
                        animationPanel.remove(currentButtons[0]);
                        currentButtons[0] = currentButtons[1];
                        currentButtons[1] = currentButtons[2];
                        currentButtons[2] = newButtonArr[0];
                        currentIndex[0] = (currentIndex[0] + 1) % size;
                    } else {
                        animationPanel.remove(currentButtons[2]);
                        currentButtons[2] = currentButtons[1];
                        currentButtons[1] = currentButtons[0];
                        currentButtons[0] = newButtonArr[0];
                        currentIndex[0] = (currentIndex[0] - 1 + size) % size;
                    }
                    animationPanel.revalidate();
                    animationPanel.repaint();
                    isAnimating[0] = false;
                    // Setze den Fokus explizit auf den mittleren (aktiven) Knopf
                    currentButtons[1].requestFocusInWindow();
                }
            }
        });
        timer.start();
    }

    // Alternative startMainscreen-Methode mit voreingestellten Spielen (unverändert)
    public static void startMainscreen(GameSelectionListener listener) {
        startMainscreen(listener, Arrays.asList("Mario", "MonkeyType", "Snake", "Speedrun", "Tetris"));
    }
    
}

class RoundedButton extends JButton {
    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);  // Unterdrückt den extra Fokusrahmen um den Text
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                requestFocusInWindow();  // Beim Hover den Fokus setzen
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        // Parameter 30 = Rundungsradius, kann angepasst werden
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        g2d.dispose();
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isFocusOwner()) {
            // Beim Fokussieren wird ein dickerer roter Rahmen gezeichnet (Stroke 5)
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(5));
            g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
        } else if (isBorderPainted()) {
            g2d.setColor(getForeground());
            g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
        }
        g2d.dispose();
    }
}