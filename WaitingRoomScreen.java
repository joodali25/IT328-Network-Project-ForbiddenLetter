import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Animated waiting room where players wait for the match to start.
 * Uses Graphics2D to draw a virtual table and floating player icons.
 */
public class WaitingRoomScreen extends JPanel {
    private List<String> currentPlayers = new ArrayList<>();
    private JLabel statusLabel;
    private JLabel timerLabel;  // Displays the countdown timer
    
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

    /**
     * Constructs the WaitingRoomScreen and initializes animation timers and UI layout.
     */
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
        
        // 1. Header Section: Title and Countdown Timer
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0));

        JLabel title = new JLabel("Waiting Room", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 48));
        title.setForeground(offWhite); 
       
        timerLabel = new JLabel("", JLabel.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 45));
        timerLabel.setForeground(new Color(255, 100, 100));
        timerLabel.setVisible(false);
        
        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.add(timerLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel: The custom-drawn game table area
        JPanel gameTablePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawWaitingArea(g2d, getWidth(), getHeight());
            }
        };
        gameTablePanel.setOpaque(false); 
        add(gameTablePanel, BorderLayout.CENTER);

        // 3. Footer Section: Status message and the Leave button
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        statusLabel = new JLabel("The game will start once the room is full...", JLabel.CENTER);
        statusLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        statusLabel.setForeground(darkOffWhite);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // --- STYLED LEAVE BUTTON START ---
        Color mutedRed = new Color(160, 45, 45);   
        Color hoverRed = new Color(120, 30, 30);   
        Color borderColor = new Color(40, 20, 15); // Dark roasted brown for borders

        JButton leaveButton = new JButton("Leave Room") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) g2.translate(1, 1);
                
                int arc = 20;
                int w = getWidth() - 5;
                int h = getHeight() - 5;

                // Draw button shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(4, 4, w, h, arc, arc);

                // Draw button background based on hover state
                g2.setColor(getModel().isRollover() ? hoverRed : mutedRed);
                g2.fillRoundRect(2, 2, w, h, arc, arc);

                // Draw button border
                g2.setStroke(new BasicStroke(2.0f));
                g2.setColor(borderColor);
                g2.drawRoundRect(2, 2, w, h, arc, arc);

                // Draw centered button text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Serif", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        // Standard button setup
        leaveButton.setPreferredSize(new Dimension(180, 50)); 
        leaveButton.setContentAreaFilled(false);
        leaveButton.setBorderPainted(false);
        leaveButton.setFocusPainted(false);
        leaveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        leaveButton.addActionListener(e -> {
            Container parent = getTopLevelAncestor();
            if (parent instanceof ClientGUI) {
                ((ClientGUI) parent).handleLeaveWaitingRoom();
            }
        });

        // Wrapper ensures the button does not stretch to fill the area
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(leaveButton);

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(buttonWrapper, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
        // --- STYLED LEAVE BUTTON END ---

        // Animation Timer to create the floating effect for player icons
        animationTimer = new Timer(50, e -> {
            angle += 0.35; 
            animationOffset = (int) (Math.sin(angle) * 3);
            repaint();
        });
        animationTimer.start();
    }

    /**
     * Updates the player list and manages the room's countdown logic.
     * @param players List of player names currently in the waiting room.
     */
    public void updateWaitingPlayers(List<String> players) {
        this.currentPlayers = players;
        
        // Start 30-second countdown when at least 2 players are present
        if (players.size() == 2 && !isCountdownActive) {
            startCountdown(10);//it was 30 but changed it for thr itfAIR
        }
        
        // Handle full room state
        if (players.size() == 4) {
            stopCountdown();
            statusLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
            statusLabel.setForeground(new Color(255, 223, 128)); 
            statusLabel.setText("Room is full! Starting the game...");
            timerLabel.setVisible(false);
        } else if (players.size() < 2) {
            stopCountdown();
            timerLabel.setVisible(false);
            statusLabel.setText("Waiting for more players...");
        }
        repaint();
    }
    
    /**
     * Starts the countdown timer and handles visual updates (color/font changes).
     * @param seconds Initial time in seconds.
     */
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
                
                // Highlight final 3 seconds in red
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
            }
        });
        countdownTimer.start();
    }
    
    /**
     * Stops the current countdown and resets the active flag.
     */
    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        isCountdownActive = false;
    }
    
    /**
     * Resets the entire screen to its initial "waiting" state.
     */
    public void resetCountdown() {
        stopCountdown();
        remainingSeconds = 0;
        timerLabel.setVisible(false);
        timerLabel.setText("");
        statusLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        statusLabel.setForeground(darkOffWhite);
        statusLabel.setText("Waiting for players...");
    }
    
    /**
     * Triggered when the server signals the game has officially started.
     */
    public void onGameStarted() {
        stopCountdown();
        timerLabel.setText("STARTED!");
        timerLabel.setForeground(new Color(100, 255, 100));
        statusLabel.setText("Game is starting...");
        new Timer(2000, e -> {
            timerLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        }).start();
    }

    /**
     * Renders the central table and player positions.
     */
    private void drawWaitingArea(Graphics2D g2d, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int rectW = 600;
        int rectH = 300;
        int rectX = (width - rectW) / 2;
        int rectY = (height - rectH) / 2;
        
        // Draw the virtual table
        g2d.setColor(new Color(70, 45, 25));
        g2d.fill(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 20, 20));
        g2d.setColor(new Color(20, 10, 5)); 
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 20, 20));
        
        // Predefined player icon coordinates
        int[][] positions = {{rectX + 150, rectY + 75},{rectX + 450, rectY + 75},{rectX + 150, rectY + 225},{rectX + 450, rectY + 225}};
        for (int i = 0; i < 4; i++) {
            boolean isPresent = (i < currentPlayers.size());
            String playerName = isPresent ? currentPlayers.get(i) : "Waiting...";
            drawPlayer(g2d, positions[i][0], positions[i][1], isPresent, playerName);
        }
    }

    /**
     * Draws an individual player icon (head, body, and shadow).
     */
    private void drawPlayer(Graphics2D g2d, int x, int y, boolean isPresent, String name) {
        int headSize = 40;
        int bodyW = 50;
        int bodyH = 60;
        int moveY = isPresent ? animationOffset : 0;
        
        if (isPresent) {
            // Draw floating shadow
            g2d.setColor(new Color(0, 0, 0, 50)); 
            g2d.fillOval(x - bodyW/2, y + 25, bodyW, 15); 
            g2d.setColor(new Color(165, 124, 0)); // Golden/Amber color for active players
        } else {
            g2d.setColor(new Color(40, 40, 40, 120)); // Muted gray for empty slots
        }
        
        // Draw Head and Body
        g2d.fill(new Ellipse2D.Double(x - headSize / 2, (y + moveY) - bodyH - headSize / 2, headSize, headSize));
        g2d.fill(new RoundRectangle2D.Double(x - bodyW / 2, (y + moveY) - bodyH / 2, bodyW, bodyH, 15, 15));
        
        // Draw Outlines
        g2d.setColor(new Color(20, 20, 20));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(x - headSize / 2, (y + moveY) - bodyH - headSize / 2, headSize, headSize));
        g2d.draw(new RoundRectangle2D.Double(x - bodyW / 2, (y + moveY) - bodyH / 2, bodyW, bodyH, 15, 15));
        
        // Draw Player Name
        g2d.setFont(new Font("Serif", isPresent ? Font.BOLD : Font.ITALIC, 17));
        g2d.setColor(offWhite);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(name, x - fm.stringWidth(name) / 2, y + bodyH / 2 + 30);
    }

    /**
     * Renders the background image onto the panel.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
