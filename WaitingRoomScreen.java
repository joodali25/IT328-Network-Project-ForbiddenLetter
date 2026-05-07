import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Animated waiting room where players wait for the match to start.
 * Uses Graphics2D to draw a virtual table and player icons.
 */
public class WaitingRoomScreen extends JPanel {
    private List<String> currentPlayers = new ArrayList<>();
    private JLabel statusLabel;
    private JLabel timerLabel;  // for timer
    
    // Theme Colors
    private final Color offWhite = new Color(245, 241, 222); 
    private final Color darkOffWhite = new Color(200, 195, 180); 
    private Image backgroundImage;

    // Animation Variables
    private int animationOffset = 0; 
    private double angle = 0; // Angle used for smooth sine-wave floating effect
    private Timer animationTimer;

    // Timer Variables
    private Timer countdownTimer;
    private int remainingSeconds = 0;
    private boolean isCountdownActive = false;

    public WaitingRoomScreen() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Load background image
        try {
            backgroundImage = new ImageIcon("src/backgroundWaittingRoom.png").getImage();
            if (backgroundImage.getWidth(null) == -1) {
                backgroundImage = new ImageIcon("backgroundWaittingRoom.png").getImage();
            }
        } catch (Exception e) {
            System.out.println("Background error in WaitingRoom");
        }
        
        // 1. Header Title + timer 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0));

        
        JLabel title = new JLabel("Waiting Room", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 48));
        title.setForeground(offWhite); 
       
        //
        timerLabel = new JLabel("", JLabel.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 45));
        timerLabel.setForeground(new Color(255, 100, 100));
        timerLabel.setVisible(false);
        
        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.add(timerLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel: The drawing area for the table and players
        JPanel gameTablePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawWaitingArea(g2d, getWidth(), getHeight());
            }
        };
        gameTablePanel.setOpaque(false); 
        add(gameTablePanel, BorderLayout.CENTER);

        // 3. Footer Status Label
        statusLabel = new JLabel("The game will start once the room is full...", JLabel.CENTER);
        statusLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        statusLabel.setForeground(darkOffWhite);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));
        add(statusLabel, BorderLayout.SOUTH);

        // Animation Timer: Runs every 50ms to create the floating effect
        animationTimer = new Timer(50, e -> {
            angle += 0.35; 
            animationOffset = (int) (Math.sin(angle) * 3); // Moves up and down by 3 pixels
            repaint();
        });
        animationTimer.start();
    }

    // Updates the list of players and changes UI style if the room is full.
    public void updateWaitingPlayers(List<String> players) {
        this.currentPlayers = players;
        
        if (players.size() == 2 && !isCountdownActive) {
            // starting timer when the second player join
            startCountdown(30);
        }
        
        if (players.size() == 4) {
            // stop timer if number of player is complet
            stopCountdown();
            statusLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
            statusLabel.setForeground(new Color(255, 223, 128)); // Golden color
            statusLabel.setText("Room is full! Starting the game...");
            timerLabel.setVisible(false);
            // if player less than 2 head the timer
        } else if (players.size() < 2) {
            stopCountdown();
            timerLabel.setVisible(false);
            statusLabel.setText("Waiting for more players...");
        }
        
        repaint();
    }
    
    private void startCountdown(int seconds) {
        stopCountdown();
        
        remainingSeconds = seconds;
        isCountdownActive = true;
        
        
        timerLabel.setText(String.valueOf(remainingSeconds));
        timerLabel.setForeground(offWhite); 
        timerLabel.setVisible(true);
        
        countdownTimer = new Timer(1000, e -> {
            if (remainingSeconds > 0 && isCountdownActive) {
                remainingSeconds--;
                timerLabel.setText(String.valueOf(remainingSeconds));
                // color cheinging in last 3 second
                if (remainingSeconds <= 3 && remainingSeconds > 0) {
                    timerLabel.setForeground(Color.RED);
                    
                    timerLabel.setFont(new Font("Serif", Font.BOLD, 56));
                } else if (remainingSeconds > 3) {
                    timerLabel.setForeground(offWhite); 
                    timerLabel.setFont(new Font("Serif", Font.BOLD, 48)); 
                }
                
            } else if (remainingSeconds == 0 && isCountdownActive) {
                                stopCountdown();
                timerLabel.setText("START!");
                timerLabel.setForeground(new Color(100, 255, 100)); 
                statusLabel.setText("Time's up! Starting the game...");
                
                new Timer(2000, evt -> {
                    timerLabel.setVisible(false);
                    ((Timer)evt.getSource()).stop();
                }).start();
                
                // هنا يمكن إضافة استدعاء لبدء اللعبة
                // onCountdownFinished();
            }
        });
        countdownTimer.start();
    }
    
    // إيقاف العداد التنازلي
    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        isCountdownActive = false;
    }
    
    public void resetCountdown() {
        stopCountdown();
        remainingSeconds = 0;
        timerLabel.setVisible(false);
        timerLabel.setText("");
        statusLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        statusLabel.setForeground(darkOffWhite);
        statusLabel.setText("Waiting for players...");
    }
    
    public void onGameStarted() {
        stopCountdown();
        timerLabel.setText("STARTED!");
        timerLabel.setForeground(new Color(100, 255, 100));
        statusLabel.setText("Game is starting...");
        // يمكن إخفاء العداد بعد ثانيتين
        new Timer(2000, e -> {
            timerLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        }).start();
    }
    // Draws the wooden table and divides it into 4 slots.
    private void drawWaitingArea(Graphics2D g2d, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rectW = 600;
        int rectH = 300;
        int rectX = (width - rectW) / 2;
        int rectY = (height - rectH) / 2;

        // Draw Table Body
        g2d.setColor(new Color(70, 45, 25));
        g2d.fill(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 20, 20));

        // Draw Table Outline
        g2d.setColor(new Color(20, 10, 5)); 
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 20, 20));

        // Player Slot Positions (X, Y)
        int[][] positions = {
                {rectX + 150, rectY + 75},
                {rectX + 450, rectY + 75},
                {rectX + 150, rectY + 225},
                {rectX + 450, rectY + 225}
        };

        // Draw each of the 4 player slots
        for (int i = 0; i < 4; i++) {
            boolean isPresent = (i < currentPlayers.size());
            String playerName = isPresent ? currentPlayers.get(i) : "Waiting...";
            drawPlayer(g2d, positions[i][0], positions[i][1], isPresent, playerName);
        }
    }

    // Draws an individual player icon with a floating animation if they are present.
    private void drawPlayer(Graphics2D g2d, int x, int y, boolean isPresent, String name) {
        int headSize = 40;
        int bodyW = 50;
        int bodyH = 60;
        
        int moveY = isPresent ? animationOffset : 0; // Apply animation if player joined

        if (isPresent) {
            // Draw a subtle shadow under the player
            g2d.setColor(new Color(0, 0, 0, 50)); 
            g2d.fillOval(x - bodyW/2, y + 25, bodyW, 15); 
            g2d.setColor(new Color(165, 124, 0)); // Golden body color
        } else {
            g2d.setColor(new Color(40, 40, 40, 120)); // Grayish for empty slots
        }

        // Draw Head and Body (using moveY for animation)
        g2d.fill(new Ellipse2D.Double(x - headSize / 2, (y + moveY) - bodyH - headSize / 2, headSize, headSize));
        g2d.fill(new RoundRectangle2D.Double(x - bodyW / 2, (y + moveY) - bodyH / 2, bodyW, bodyH, 15, 15));

        // Draw Outlines
        g2d.setColor(new Color(20, 20, 20));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(x - headSize / 2, (y + moveY) - bodyH - headSize / 2, headSize, headSize));
        g2d.draw(new RoundRectangle2D.Double(x - bodyW / 2, (y + moveY) - bodyH / 2, bodyW, bodyH, 15, 15));

        // Draw Player Name (Static position, no moveY)
        g2d.setFont(new Font("Serif", isPresent ? Font.BOLD : Font.ITALIC, 17));
        g2d.setColor(offWhite);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(name, x - fm.stringWidth(name) / 2, y + bodyH / 2 + 30);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
