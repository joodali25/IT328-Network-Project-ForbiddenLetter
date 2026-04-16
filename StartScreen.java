import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The initial landing screen of the game.
 * Features the game title and a START button to begin.
 */
public class StartScreen extends JPanel {

    // Theme Colors
    private final Color titleColor = new Color(35, 30, 25);     // Dark charcoal for the title
    private final Color buttonGreen = new Color(85, 107, 47);   // Olive green for the button
    private final Color buttonHover = new Color(65, 82, 36);    // Darker green for hover effect
    private Image backgroundImage;

    public StartScreen(ActionListener startAction) {
        // Load the background image
        try {
            backgroundImage = new ImageIcon("src/background.png").getImage();
            if (backgroundImage.getWidth(null) == -1) {
                backgroundImage = new ImageIcon("background.png").getImage();
            }
        } catch (Exception e) {
            System.out.println("Image error in StartScreen");
        }

        // Use BorderLayout for the main container
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Center panel to hold title, subtitle, and button using BoxLayout (Vertical)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 1. Main Game Title
        JLabel titleLabel = new JLabel("FORBIDDEN LETTER");
        titleLabel.setFont(new Font("Imprint MT Shadow", Font.BOLD, 52));
        titleLabel.setForeground(titleColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Subtitle
        JLabel subTitleLabel = new JLabel("A GAME OF WITS AND WORDS");
        subTitleLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        subTitleLabel.setForeground(titleColor);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Custom Styled START Button
        JButton startButton = new JButton("START") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                int round = 20;

                // Draw the dark outer border/shadow
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(0, 0, w, h, round, round);

                // Draw the main button body
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, w-4, h-4, round, round);
                
                // Draw the button text
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(getForeground());
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };

        // Button Properties
        startButton.setFont(new Font("Serif", Font.BOLD, 36));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(buttonGreen);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false); 
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Fixed dimensions for the button
        startButton.setPreferredSize(new Dimension(250, 80));
        startButton.setMaximumSize(new Dimension(250, 80));
        
        // Mouse Listeners for Hover and Cursor effects
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startButton.setBackground(buttonHover);
                startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                startButton.setBackground(buttonGreen);
                startButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        startButton.addActionListener(startAction);

        // Layout Organization (Adding vertical glues and struts to center content)
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10)); 
        centerPanel.add(subTitleLabel);
        centerPanel.add(Box.createVerticalStrut(35)); 
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(245, 241, 222));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
