/**
 * @author Hanan Alghamdi
 * Modified for full visual integration with StartScreen (Vintage Style)
 */

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientGUI extends JFrame {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9090;

    // الألوان المتوافقة مع StartScreen
    private final Color creamBG = new Color(245, 241, 222);
    private final Color darkNavy = new Color(44, 51, 62);
    private final Color buttonGreen = new Color(112, 146, 149);

    private Socket socket;
    private PrintWriter out;
    private ServerConnection serverConnection;
    private Thread connectionThread;

    private JTextField usernameField;
    private JButton connectButton;
    private JButton playButton;

    private DefaultListModel<String> connectedPlayersModel;
    private DefaultListModel<String> waitingPlayersModel;

    private JList<String> connectedPlayersList;
    private JList<String> waitingPlayersList;

    private JTextArea statusArea;

    private boolean connected = false;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public ClientGUI() {
        setTitle("Forbidden Letter - Client");
        setSize(750, 550); // زدنا الحجم شوي عشان المساحة الفنية
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // بناء شاشة اللوبي (المرتبطة بالتصميم الجديد)
        JPanel lobbyPanel = new JPanel(new BorderLayout(15, 15));
        lobbyPanel.setBackground(creamBG);
        lobbyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setupLobbyLayout(lobbyPanel);

        // ربط شاشة البداية
        StartScreen startScreen = new StartScreen(e -> cardLayout.show(mainPanel, "LOBBY"));

        mainPanel.add(startScreen, "START");
        mainPanel.add(lobbyPanel, "LOBBY");

        add(mainPanel);
        cardLayout.show(mainPanel, "START");

        registerActions();
    }

    private void initComponents() {
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Serif", Font.PLAIN, 16));
        
        connectButton = new JButton("Connect");
        styleButton(connectButton);
        
        playButton = new JButton("Play");
        styleButton(playButton);
        playButton.setEnabled(false);

        connectedPlayersModel = new DefaultListModel<>();
        waitingPlayersModel = new DefaultListModel<>();

        connectedPlayersList = new JList<>(connectedPlayersModel);
        styleList(connectedPlayersList);

        waitingPlayersList = new JList<>(waitingPlayersModel);
        styleList(waitingPlayersList);

        statusArea = new JTextArea(5, 20);
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(255, 255, 250)); // لون ورقي فاتح
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    // دالة لتنسيق الأزرار بنفس روح StartScreen
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setBackground(buttonGreen);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(darkNavy, 1));
    }

    // دالة لتنسيق القوائم
    private void styleList(JList<String> list) {
        list.setFont(new Font("Serif", Font.BOLD, 18));
        list.setBackground(new Color(255, 255, 252));
        list.setForeground(darkNavy);
        list.setSelectionBackground(buttonGreen);
    }

    private void setupLobbyLayout(JPanel lobbyPanel) {
        // الجزء العلوي: تسجيل الدخول
        JPanel topPanel = new JPanel();
        topPanel.setBackground(creamBG);
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Serif", Font.BOLD, 18));
        userLabel.setForeground(darkNavy);
        topPanel.add(userLabel);
        topPanel.add(usernameField);
        topPanel.add(connectButton);
        topPanel.add(playButton);

        // الجزء الأوسط: القوائم (بشكل بطاقات فنية)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(creamBG);

        centerPanel.add(createStyledListPanel("CONNECTED PLAYERS", connectedPlayersList));
        centerPanel.add(createStyledListPanel("WAITING ROOM", waitingPlayersList));

        // الجزء السفلي: الحالة
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(creamBG);
        TitledBorder statusBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(darkNavy), "SYSTEM STATUS");
        statusBorder.setTitleFont(new Font("Serif", Font.BOLD, 12));
        bottomPanel.setBorder(statusBorder);
        bottomPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        lobbyPanel.add(topPanel, BorderLayout.NORTH);
        lobbyPanel.add(centerPanel, BorderLayout.CENTER);
        lobbyPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // دالة مساعدة لإنشاء لوحات القوائم بشكل أنيق
    private JPanel createStyledListPanel(String title, JList<String> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(creamBG);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(darkNavy, 2), title);
        border.setTitleFont(new Font("Serif", Font.BOLD, 14));
        border.setTitleJustification(TitledBorder.CENTER);
        panel.setBorder(border);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private void registerActions() {
        connectButton.addActionListener(e -> connectToServer());
        playButton.addActionListener(e -> sendPlayRequest());
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            appendStatus("Please enter a username first.");
            return;
        }
        if (connected) {
            appendStatus("You are already connected.");
            return;
        }
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            serverConnection = new ServerConnection(socket, this);
            connectionThread = new Thread(serverConnection);
            connectionThread.start();
            out.println("CONNECT:" + username);
            connected = true;
            connectButton.setEnabled(false);
            usernameField.setEditable(false);
            playButton.setEnabled(true);
            appendStatus("Connected to server successfully.");
        } catch (IOException ex) {
            appendStatus("Failed to connect to server: " + ex.getMessage());
        }
    }

    private void sendPlayRequest() {
        if (!connected || out == null) {
            appendStatus("You must connect first.");
            return;
        }
        out.println("Play:");
        appendStatus("Play request sent.");
    }

    public void updateConnectedPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            connectedPlayersModel.clear();
            for (String player : players) {
                if (!player.trim().isEmpty()) {
                    connectedPlayersModel.addElement(" • " + player.trim());
                }
            }
        });
    }

    public void updateWaitingPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            waitingPlayersModel.clear();
            for (String player : players) {
                if (!player.trim().isEmpty()) {
                    waitingPlayersModel.addElement(" » " + player.trim());
                }
            }
        });
    }

    public void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append("> " + message + "\n");
        });
    }

    public List<String> parseNames(String data) {
        if (data == null || data.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(data.split(","));
    }
}
