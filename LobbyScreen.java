import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/*
 * The Lobby screen where players can see who is online and check the game status.
 */
public class LobbyScreen extends JPanel {
    private DefaultListModel<String> connectedPlayersModel;
    private JPanel waitingRoomPanel; 
    private JTextArea statusArea;
    private JButton playButton;
    private Image backgroundImage;

    // Theme Colors (Consistent with the Vintage Background)
    private final Color antiqueTitle = new Color(35, 30, 25);     // Dark charcoal for titles
    private final Color cardBG = new Color(252, 248, 232);        // Classic paper color
    private final Color sageGreen = new Color(85, 107, 47);       // Muted olive for buttons
    private final Color buttonHover = new Color(65, 82, 36);      // Darker olive for hover effect
    private final Color brownBorder = new Color(101, 67, 33);     // Deep brown for borders

    public LobbyScreen(java.awt.event.ActionListener playAction) {
        // Load the main background image
        try {
            backgroundImage = new ImageIcon("src/background.png").getImage();
            if (backgroundImage.getWidth(null) == -1) {
                backgroundImage = new ImageIcon("background.png").getImage();
            }
        } catch (Exception e) {
            System.out.println("Background error in LobbyScreen");
        }

        // Using BorderLayout with wide empty borders for centering the UI
        setLayout(new BorderLayout(30, 30));
        setBorder(BorderFactory.createEmptyBorder(50, 180, 50, 180));
        setOpaque(false);
        
        initComponents(playAction);
        setupLayout();
    }

    private void initComponents(java.awt.event.ActionListener playAction) {
        connectedPlayersModel = new DefaultListModel<>();
        
        waitingRoomPanel = new JPanel();
        waitingRoomPanel.setLayout(new BoxLayout(waitingRoomPanel, BoxLayout.Y_AXIS));
        waitingRoomPanel.setOpaque(false);

        // Initialize the premium styled Play button
        playButton = createPremiumButton("PLAY", true);
        playButton.addActionListener(playAction);

        // Text area to show real-time game room status
        statusArea = new JTextArea("Status: Connected! Press PLAY to join the game.");
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setBackground(cardBG);
        statusArea.setFont(new Font("Serif", Font.BOLD, 18));
    }

    private void setupLayout() {
        // 1. Header Title
        JLabel title = new JLabel("GAME LOBBY", JLabel.CENTER);
        title.setFont(new Font("Imprint MT Shadow", Font.BOLD, 48));
        title.setForeground(antiqueTitle);
        add(title, BorderLayout.NORTH);

        // 2. Center Section: A card displaying connected players
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JList<String> playerList = new JList<>(connectedPlayersModel);
        playerList.setFont(new Font("Serif", Font.PLAIN, 20));
        
        centerPanel.add(createCard("CONNECTED PLAYERS", new JScrollPane(playerList)), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer Section: Status display and the Play button
        JPanel footer = new JPanel(new BorderLayout(25, 0));
        footer.setOpaque(false);
        
        JPanel statusWrapper = new JPanel(new BorderLayout());
        statusWrapper.setBackground(cardBG);
        statusWrapper.setBorder(new CompoundBorder(new LineBorder(brownBorder, 2), new EmptyBorder(15,15,15,15)));
        statusWrapper.add(statusArea, BorderLayout.CENTER);

        footer.add(statusWrapper, BorderLayout.CENTER);
        footer.add(playButton, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Updates the main list of all players currently connected to the server.
     */
    public void updateConnected(List<String> players) {
        connectedPlayersModel.clear();
        for (int i = 0; i < players.size(); i++) {
            connectedPlayersModel.addElement((i + 1) + ". " + players.get(i));
        }
    }

    /**
     * Updates the text area with the latest waiting room information (Names & Count).
     */
    public void updateLobbyStatus(List<String> waitingPlayers, String myName) {
        int total = waitingPlayers.size();
        statusArea.setText(""); 
        statusArea.setOpaque(false); 

        Color statusTextColor = new Color(45, 45, 48);

        if (total == 0) {
            statusArea.setText("STATUS: Waiting Room is empty [0/4].\nBe the first to join!");
            playButton.setText("PLAY");
            playButton.setEnabled(true);
        } else if (total < 4) {
            String names = String.join(", ", waitingPlayers);
            statusArea.setText("STATUS: Waiting Room [ " + total + " / 4 ].\nInside: " + names);
            playButton.setText("PLAY");
            playButton.setEnabled(true);
        } else {
            String names = String.join(", ", waitingPlayers);
            statusArea.setText("STATUS: Room is FULL [4/4].\nPlaying now: " + names);
            playButton.setText("FULL");
            playButton.setEnabled(false);
        }
        
        statusArea.setForeground(statusTextColor);
        revalidate();
        repaint();
    }

    /**
     * Helper method to create a consistent styled card for UI sections.
     */
    private JPanel createCard(String title, Component comp) {
        JPanel p = new JPanel(new BorderLayout());
        Color vintageBeige = new Color(245, 241, 222); 
        p.setBackground(vintageBeige);
        p.setOpaque(true); 
        
        TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(brownBorder, 2), title);
        b.setTitleFont(new Font("Serif", Font.BOLD, 22));
        b.setTitleColor(brownBorder);
        b.setTitleJustification(TitledBorder.CENTER);
        
        p.setBorder(new CompoundBorder(b, BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        if (comp instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) comp;
            scroll.getViewport().setBackground(vintageBeige);
            scroll.setBorder(null);
            
            Component view = scroll.getViewport().getView();
            if (view instanceof JList) {
                view.setBackground(vintageBeige);
                ((JList<?>) view).setFont(new Font("Serif", Font.PLAIN, 20));
                ((JList<?>) view).setForeground(new Color(45, 45, 48));
            }
        }
        
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    /**
     * Custom painting for the button to include shadows, rounded corners, and hover effects.
     */
    private JButton createPremiumButton(String text, boolean enabled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) g2.translate(1, 1);

                int arc = 20;
                // Draw drop shadow
                g2.setColor(new Color(30, 30, 30, 40));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, arc, arc);

                // Determine button color based on state (Enabled vs Hover vs Disabled)
                Color mainCol = isEnabled() ? sageGreen : new Color(180, 175, 160); 
                if (getModel().isRollover() && isEnabled()) mainCol = buttonHover;
                
                g2.setColor(mainCol);
                g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);

                // Draw button outline
                g2.setColor(new Color(50, 35, 25));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);

                // Draw button text in center
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()-fm.getHeight())/2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(180, 60));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setEnabled(enabled);
        btn.setFont(new Font("Serif", Font.BOLD, 22));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    public void appendStatus(String msg) { statusArea.setText("Status: " + msg); }
    public void disablePlayButton() { playButton.setEnabled(false); }
}
