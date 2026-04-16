import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
/**
 * Main GUI Controller for the Client application.
 * Manages screen transitions using CardLayout and handles Server-Client communication.
 */
public class ClientGUI extends JFrame {

    private static final int SERVER_PORT = 9090;
    private Socket socket;
    private PrintWriter out;
    private ServerConnection serverConnection;
    private String currentUsername;
    
 // Screens managed by CardLayout
    private ConnectScreen connectScreen;
    private LobbyScreen lobbyScreen;
    private StartScreen startScreen;
    private WaitingRoomScreen waitingRoomScreen;

    private CardLayout cardLayout; // Used to switch between different screens
    private JPanel mainPanel;      // The container that holds all screens
    private boolean connected = false; 

    public ClientGUI() {

        setTitle("Forbidden Letter - Client");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        // Setting up screen transitions (Button Actions)
        startScreen = new StartScreen(e -> cardLayout.show(mainPanel, "CONNECT"));

        connectScreen = new ConnectScreen(e ->
                connectToServer(connectScreen.getIP(), connectScreen.getUsername())
        );

        lobbyScreen = new LobbyScreen(e -> sendPlayRequest());

        waitingRoomScreen = new WaitingRoomScreen();
     // Adding screens to the CardLayout manager
        mainPanel.add(startScreen, "START");
        mainPanel.add(connectScreen, "CONNECT");
        mainPanel.add(lobbyScreen, "LOBBY");
        mainPanel.add(waitingRoomScreen, "WAITING");

        add(mainPanel);

        cardLayout.show(mainPanel, "START");// Start with the first screen
    }
    
    // Establishes a socket connection to the server
    public void connectToServer(String ip, String username) {
        if (username.isEmpty() || ip.isEmpty()) {
            showErrorDialog("Username or IP cannot be empty.");
            return;
        }

        this.currentUsername = username;

        try {
            socket = new Socket(ip, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Tell the server who is connecting
            out.println("CONNECT:" + currentUsername);
         // Run a separate thread to keep listening for server messages
            serverConnection = new ServerConnection(socket, this);
            new Thread(serverConnection).start();

        } catch (IOException ex) {
            showErrorDialog("Failed to connect: " + ex.getMessage());
        }
    }
    
 // Helper method to show error pop-ups
    public void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }
    
    // Update the list of players currently in the Lobby
    public void updateConnectedPlayers(List<String> players) {
        SwingUtilities.invokeLater(() ->
                lobbyScreen.updateConnected(players)
        );
    }
    
    // When clicking 'PLAY', inform the server and move to waiting screen
    private void sendPlayRequest() {
        if (out != null) {
            out.println("Play:");
            cardLayout.show(mainPanel, "WAITING");
        }
    }
    
    	// Updates the waiting room and moves the player to the WAITING screen
    	public void updateWaitingPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            waitingRoomScreen.updateWaitingPlayers(players);
            lobbyScreen.updateLobbyStatus(players, currentUsername);
         // If server says we are in the list, make sure we see the waiting screen
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
    
 // Called when server confirms the connection is successful
    public void showSuccessAndNavigate() {
    if (this.connected) return; 

        SwingUtilities.invokeLater(() -> {
            this.connected = true;
            JOptionPane.showMessageDialog(this, "Connected successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "LOBBY");
        });
    }
    public void showLobby() { cardLayout.show(mainPanel, "LOBBY"); }

 // Show error if the username is taken or server is down
    public void handleConnectionError(String errorMessage) {
        this.connected = false; 
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
