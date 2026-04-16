import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StartScreen extends JPanel {

    // 1. الألوان المحدثة (أسود للعنوان، وأخضر مشبع قليلاً للزر)
	private final Color titleColor = new Color(45, 45, 48);
	private final Color buttonGreen = new Color(60, 115, 125); 
    private final Color buttonHover = new Color(70, 110, 115);
    private Image backgroundImage;

    public StartScreen(ActionListener startAction) {
        try {
            backgroundImage = new ImageIcon("src/background.png").getImage();
            if (backgroundImage.getWidth(null) == -1) {
                backgroundImage = new ImageIcon("background.png").getImage();
            }
        } catch (Exception e) {
            System.out.println("Image error");
        }

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("FORBIDDEN LETTER");
        // العنوان بخط Imprint MT Shadow (المجوف)
        titleLabel.setFont(new Font("Imprint MT Shadow", Font.BOLD, 52));
        titleLabel.setForeground(titleColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("A GAME OF WITS AND WORDS");
        subTitleLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        subTitleLabel.setForeground(titleColor);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. زر START المطور (حجم أصغر قليلاً 250x80)
        JButton startButton = new JButton("START") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                int round = 20;

                // الإطار الغامق (الظل)
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(0, 0, w, h, round, round);

                // جسم الزر
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, w-4, h-4, round, round);
                
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(getForeground());
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };

        startButton.setFont(new Font("Serif", Font.BOLD, 36)); // صغرنا الخط قليلاً ليناسب حجم الزر
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(buttonGreen);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false); 
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // الحجم الجديد المحدث
        startButton.setPreferredSize(new Dimension(250, 80));
        startButton.setMaximumSize(new Dimension(250, 80));
        
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

        // توزيع العناصر (تقليل المسافات لرفع الزر)
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10)); 
        centerPanel.add(subTitleLabel);
        centerPanel.add(Box.createVerticalStrut(35)); // كانت 70، قللتها لرفع الزر
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(245, 241, 222));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
