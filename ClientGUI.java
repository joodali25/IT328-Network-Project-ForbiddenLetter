import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientGUI extends JFrame {

    private static final int SERVER_PORT = 9090;

    private Socket socket;
    private PrintWriter out;
    private ServerConnection serverConnection;

    private String currentUsername;

    private ConnectScreen connectScreen;
    private LobbyScreen lobbyScreen;
    private StartScreen startScreen;
    private WaitingRoomScreen waitingRoomScreen;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public ClientGUI() {

        setTitle("Forbidden Letter - Client");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        startScreen = new StartScreen(e -> cardLayout.show(mainPanel, "CONNECT"));

        connectScreen = new ConnectScreen(e ->
                connectToServer(connectScreen.getIP(), connectScreen.getUsername())
        );

        lobbyScreen = new LobbyScreen(e -> sendPlayRequest());

        waitingRoomScreen = new WaitingRoomScreen();

        mainPanel.add(startScreen, "START");
        mainPanel.add(connectScreen, "CONNECT");
        mainPanel.add(lobbyScreen, "LOBBY");
        mainPanel.add(waitingRoomScreen, "WAITING");

        add(mainPanel);

        cardLayout.show(mainPanel, "START");
    }

    public void connectToServer(String ip, String username) {

        if (username.isEmpty() || ip.isEmpty()) {
            showErrorDialog("Username or IP cannot be empty.");
            return;
        }

        this.currentUsername = username;

        try {
            socket = new Socket(ip, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("CONNECT:" + currentUsername);

            serverConnection = new ServerConnection(socket, this);
            new Thread(serverConnection).start();

            // ✔ فقط دخول اللوبي
            cardLayout.show(mainPanel, "LOBBY");

        } catch (IOException ex) {
            showErrorDialog("Failed to connect: " + ex.getMessage());
        }
    }

    public void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void updateConnectedPlayers(List<String> players) {
        SwingUtilities.invokeLater(() ->
                lobbyScreen.updateConnected(players)
        );
    }

    private void sendPlayRequest() {
        if (out != null) {
            out.println("Play:");
            cardLayout.show(mainPanel, "WAITING"); // انتقال فوري للشخص الذي ضغط الزر
        }
    }

    public void updateWaitingPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            waitingRoomScreen.updateWaitingPlayers(players);
            lobbyScreen.updateLobbyStatus(players, currentUsername);
            
            // إذا كان اللاعب أصلاً في قائمة الانتظار، تأكد أنه يرى شاشة الانتظار
            if (players.contains(currentUsername)) {
                cardLayout.show(mainPanel, "WAITING");
            }
        });
    }

    public void appendStatus(String message) {
        SwingUtilities.invokeLater(() ->
                lobbyScreen.appendStatus(message)
        );
    }

    public List<String> parseNames(String data) {
        if (data == null || data.trim().isEmpty()) return Arrays.asList();
        return Arrays.asList(data.split(","));
    }

    public void showWaitingScreen() {
        cardLayout.show(mainPanel, "WAITING");
    }
}
