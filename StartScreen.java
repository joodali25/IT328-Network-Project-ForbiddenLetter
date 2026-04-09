import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class StartScreen extends JPanel {

    // 1. الألوان الدقيقة من الصورة الـ Vintage الفخمة
    private final Color creamBG = new Color(245, 241, 222);
    private final Color darkNavy = new Color(44, 51, 62);
    private final Color buttonGreen = new Color(112, 146, 149);
    private final Color buttonHover = new Color(85, 115, 118);

    public StartScreen(ActionListener startAction) {
        // إعدادات اللوحة الرئيسية
        setLayout(new BorderLayout());
        setBackground(creamBG);
        
        // 2. إلغاء الحدود الرمادية تماماً (No Grey Borders)
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // حاوية العناصر في المنتصف
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(creamBG);

        // 3. العنوان الرئيسي (FORBIDDEN LETTER) بخط Serif عريض جداً وفخم
        JLabel titleLabel = new JLabel("FORBIDDEN LETTER");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 55));
        titleLabel.setForeground(darkNavy);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. العنوان الفرعي بخط أصغر وأنيق (A GAME OF WITS AND WORDS)
        JLabel subTitleLabel = new JLabel("A GAME OF WITS AND WORDS");
        subTitleLabel.setFont(new Font("Serif", Font.ITALIC, 18));
        subTitleLabel.setForeground(darkNavy);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 5. زر START المطور بلمسة 3D وحواف دائرية أنيقة (true)
        JButton startButton = new JButton("START");
        startButton.setFont(new Font("Serif", Font.BOLD, 40));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(buttonGreen);
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // تجميل الزر: حجم محدد وحواف دائرية أنيقة
        startButton.setPreferredSize(new Dimension(250, 80));
        startButton.setMaximumSize(new Dimension(250, 80));
        startButton.setBorder(new LineBorder(darkNavy, 1, true));
        
        // 6. إضافة تفاعل (Hover Effect) عشان يوضح إنه ينضغط
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

        // إضافة الحدث للزر
        startButton.addActionListener(startAction);

        // إضافة العناصر مع مسافات جمالية (Struts/Glue) بينها
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10)); // مسافة صغيرة للفرعي
        centerPanel.add(subTitleLabel);
        centerPanel.add(Box.createVerticalStrut(60)); // مسافة كبيرة للزر
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }
}
