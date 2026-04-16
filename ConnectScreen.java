import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConnectScreen extends JPanel {
    private JTextField usernameField;
    private JTextField ipField;
    private JButton connectButton;
    private Image backgroundImage;

    // Theme Colors (Vintage Palette)
    private final Color charcoalTitle = new Color(35, 30, 25); // Dark title color
    private final Color buttonGreen = new Color(85, 107, 47);   // Olive Green
    private final Color buttonHover = new Color(65, 82, 36);   // Darker Olive for hover
    private final Color fieldBG = new Color(255, 253, 245);    // Creamy white for input fields
    private final Color brownBorder = new Color(101, 67, 33);  // Classic brown border

    public ConnectScreen(ActionListener connectAction) {
        // Load the background image
        try {
            backgroundImage = new ImageIcon("src/background.png").getImage();
            if (backgroundImage.getWidth(null) == -1) {
                backgroundImage = new ImageIcon("background.png").getImage();
            }
        } catch (Exception e) {
            System.out.println("Background error in ConnectScreen");
        }

        // Use GridBagLayout to center all components perfectly
        setLayout(new GridBagLayout());
        setOpaque(false);
        
        // Inner panel to group elements together
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Header Title
        JLabel mainHeader = new JLabel("JOIN THE FORBIDDEN GAME");
        mainHeader.setFont(new Font("Imprint MT Shadow", Font.BOLD, 42));
        mainHeader.setForeground(charcoalTitle);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 50, 0); 
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(mainHeader, gbc);

        // Labels for Input Fields
        gbc.gridwidth = 1;
        
        JLabel userLabel = new JLabel("USERNAME: ");
        userLabel.setFont(new Font("Serif", Font.BOLD, 22));
        userLabel.setForeground(charcoalTitle);

        JLabel ipLabel = new JLabel("SERVER IP: ");
        ipLabel.setFont(new Font("Serif", Font.BOLD, 22));
        ipLabel.setForeground(charcoalTitle);

        // 2. Input Fields Initialization
        usernameField = createPremiumField(20);
        ipField = createPremiumField(20);
        ipField.setText("localhost");

        // Layout: Username Row
        gbc.gridx = 0; gbc.gridy = 1; 
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 0, 15, 15);
        contentPanel.add(userLabel, gbc);
        
        gbc.gridx = 1; 
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 15, 0);
        contentPanel.add(usernameField, gbc);

        // Layout: Server IP Row
        gbc.gridx = 0; gbc.gridy = 2; 
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 0, 15, 15);
        contentPanel.add(ipLabel, gbc);
        
        gbc.gridx = 1; 
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 15, 0);
        contentPanel.add(ipField, gbc);

        // 3. Custom Styled CONNECT Button
        connectButton = new JButton("CONNECT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Add click effect (slight translation)
                if (getModel().isPressed()) g2.translate(1, 1);

                int arc = 20;

                // Draw Button Shadow
                g2.setColor(new Color(30, 30, 30, 40));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, arc, arc);

                // Switch color based on Mouse Hover (Rollover)
                if (getModel().isRollover()) {
                    g2.setColor(buttonHover); 
                } else {
                    g2.setColor(buttonGreen);
                }
                g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
                
                // Draw Button Border
                g2.setColor(new Color(50, 35, 25));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);

                // Center the text inside the button
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                g2.setColor(Color.WHITE); 
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        // Button Properties
        connectButton.setFont(new Font("Serif", Font.BOLD, 24));
        connectButton.setForeground(Color.WHITE);
        connectButton.setPreferredSize(new Dimension(180, 60));
        connectButton.setContentAreaFilled(false);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(connectAction);

        // Change cursor to HAND when hovering over the button
        connectButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(40, 0, 0, 0); 
        contentPanel.add(connectButton, gbc);

        // Center the entire content panel on the screen
        add(contentPanel, new GridBagConstraints());
    }

    // Helper method to create styled text fields
    private JTextField createPremiumField(int cols) {
        JTextField field = new JTextField(cols);
        field.setFont(new Font("Serif", Font.PLAIN, 22));
        field.setBackground(fieldBG);
        field.setForeground(charcoalTitle);
        field.setCaretColor(charcoalTitle);
        
        // Brown border with inner padding for better look
        field.setBorder(new CompoundBorder(
            new LineBorder(brownBorder, 2), 
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw background image if available
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(245, 241, 222));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Getters to retrieve user input
    public String getUsername() { return usernameField.getText().trim(); }
    public String getIP() { return ipField.getText().trim(); }
}
