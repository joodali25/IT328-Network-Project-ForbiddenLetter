import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * GameScreen provides the main gameplay interface for the user.
 * It handles the display of game information, player logs, and user input.
 */
public class GameScreen extends JPanel {

    private JLabel topicLabel, forbiddenLabel, timerLabel, scoreLabel, levelLabel;
    private JTextField inputField;
    private JButton submitButton, leaveButton;
    private JTextArea statusArea;
    private Image backgroundImage;
    private JLabel totalGameTimerLabel;

    // Visual Constants: Colors for text and UI elements
    private final Color COLOR_TEXT_DARK = new Color(55, 40, 25);
    private final Color COLOR_PANEL = new Color(255, 248, 235);
    private final Color COLOR_SUBMIT = new Color(110, 145, 105);
    private final Color COLOR_LEAVE = new Color(170, 75, 75);

    /**
     * Constructs the GameScreen panel and initializes all UI components.
     * @param submitAction Action listener for the submit button.
     * @param leaveAction Action listener for the leave button.
     */
    public GameScreen(ActionListener submitAction, ActionListener leaveAction) {

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Loading the blurred background image
        try {
            backgroundImage = new ImageIcon("src/background_blurred.png").getImage();
        } catch (Exception e) {
            System.out.println("Background error: background_blurred.png not found");
        }

        // ================= TOP BAR =================
        // Displays current level, topic, forbidden letter, and timers.

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        JPanel topBar = new JPanel(new GridLayout(2, 3, 12, 12));
        topBar.setOpaque(false);

        levelLabel = new JLabel("Level: 1", SwingConstants.CENTER);
        topicLabel = new JLabel("Topic: -", SwingConstants.CENTER);
        forbiddenLabel = new JLabel("Forbidden: -", SwingConstants.CENTER);
        timerLabel = new JLabel("Level Time: 60s", SwingConstants.CENTER);
        totalGameTimerLabel = new JLabel("Total: 07:00", SwingConstants.CENTER);
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);

        topBar.add(createInfoCard(levelLabel));
        topBar.add(createInfoCard(topicLabel));
        topBar.add(createInfoCard(forbiddenLabel));
        topBar.add(createInfoCard(timerLabel));
        topBar.add(createInfoCard(totalGameTimerLabel));
        topBar.add(createInfoCard(scoreLabel));

        topContainer.add(topBar, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // ================= CENTER =================
        // Displays validation messages and game status history.

        statusArea = new JTextArea();

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 17));
        statusArea.setForeground(COLOR_TEXT_DARK);

        statusArea.setBackground(Color.WHITE);

        statusArea.setBorder(new EmptyBorder(18, 18, 18, 18));

        JScrollPane scrollPane = new JScrollPane(statusArea);

        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(new Color(80, 60, 40), 2),
                new EmptyBorder(5, 5, 5, 5)
        ));

        add(scrollPane, BorderLayout.CENTER);

        // ================= BOTTOM =================
        // Includes the input text field and action buttons.

        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setOpaque(false);

        inputField = new JTextField();

        inputField.setFont(new Font("Serif", Font.BOLD, 20));

        inputField.setPreferredSize(new Dimension(0, 55));

        inputField.setBorder(new CompoundBorder(
                new LineBorder(COLOR_TEXT_DARK, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));

        inputField.addActionListener(submitAction);

        submitButton = new RoundedButton(
                "Submit Word",
                COLOR_SUBMIT,
                Color.WHITE
        );

        submitButton.addActionListener(submitAction);

        leaveButton = new RoundedButton(
                "Leave Game",
                COLOR_LEAVE,
                Color.WHITE
        );

        leaveButton.addActionListener(leaveAction);

        submitButton.setPreferredSize(new Dimension(160, 55));
        leaveButton.setPreferredSize(new Dimension(160, 55));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        buttonPanel.add(submitButton);
        buttonPanel.add(leaveButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Helper method to create a stylized info card panel for displaying stats.
     * @param label The JLabel to be wrapped in the card.
     * @return A styled JPanel container.
     */
    private JPanel createInfoCard(JLabel label) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());

        inner.setBackground(COLOR_PANEL);

        inner.setBorder(new CompoundBorder(
                new LineBorder(new Color(85, 60, 35), 2),
                new EmptyBorder(12, 10, 12, 10)
        ));

        label.setFont(new Font("Serif", Font.BOLD, 18));
        label.setForeground(COLOR_TEXT_DARK);

        inner.add(label, BorderLayout.CENTER);

        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates the game information labels based on level data.
     * @param level Current game level.
     * @param topic Current word topic.
     * @param forbidden The restricted character.
     * @param timer Remaining time for the current level.
     */
    public void updateGameInfo(String level, String topic, String forbidden, String timer) {

        levelLabel.setText("Level: " + level);
        topicLabel.setText("Topic: " + topic);
        forbiddenLabel.setText("Forbidden: " + forbidden);
        timerLabel.setText("Time: " + timer + "s");
    }

    /**
     * Parses and updates the scoreboard display.
     * @param scoresRaw The raw protocol score string from the server.
     */
    public void updateScore(String scoresRaw) {

        String cleanScores = scoresRaw.replace("SCORES:", "");

        scoreLabel.setText("Score: " + cleanScores.replace(",", " | "));
    }

    /**
     * Updates the level countdown timer display.
     * @param seconds Remaining seconds.
     */
    public void updateTimer(String seconds) {

        timerLabel.setText("Time: " + seconds + "s");
    }

    /**
     * Appends a new message to the status area and scrolls to the latest update.
     * @param msg The message string to log.
     */
    public void appendStatus(String msg) {

        statusArea.append("> " + msg + "\n");

        statusArea.setCaretPosition(
                statusArea.getDocument().getLength()
        );
    }

    /**
     * Retrieves the player's input from the field and clears it for the next entry.
     * @return The trimmed word string entered by the player.
     */
    public String getInputWord() {

        String word = inputField.getText().trim();

        inputField.setText("");

        return word;
    }

    /**
     * Custom painting to render the background image on the panel.
     */
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (backgroundImage != null) {

            g.drawImage(
                    backgroundImage,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );
        }
    }

    /**
     * Updates the total session timer label.
     * @param time Formatted time string (e.g., "07:00").
     */
    public void updateTotalTimer(String time) {

        totalGameTimerLabel.setText("Total: " + time);
    }

    /**
     * A custom JButton inner class that renders rounded corners and hover effects.
     */
    private static class RoundedButton extends JButton {

        private Color bgColor;
        private Color fgColor;

        RoundedButton(String text, Color bg, Color fg) {

            super(text);

            this.bgColor = bg;
            this.fgColor = fg;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);

            setForeground(fgColor);

            setFont(new Font("Serif", Font.BOLD, 16));

            setCursor(new Cursor(Cursor.HAND_CURSOR));

            Color hoverColor = bg.brighter();

            // Handle hover state color changes
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    bgColor = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    bgColor = bg;
                    repaint();
                }
            });
        }

        /**
         * Custom painting to draw the rounded background shape.
         */
        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2d.setColor(bgColor);

            g2d.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    22,
                    22
            );

            super.paintComponent(g2d);

            g2d.dispose();
        }
    }
}
