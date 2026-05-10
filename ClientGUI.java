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
    private GameScreen gameScreen;
    
    private CardLayout cardLayout; // Used to switch between different screens
    private JPanel mainPanel;      // The container that holds all screens
    private boolean connected = false; 

    /**
     * Initializes the client GUI, sets up CardLayout, and creates all screen components.
     */
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

        cardLayout.show(mainPanel, "START"); // Start with the first screen

        gameScreen = new GameScreen(
                e -> sendWord(), // Action for word submission
                e -> leaveGame() // Action for leaving the game
            );
            mainPanel.add(gameScreen, "GAME");
            
            // Window listener ensures the server is notified even if the player closes the window abruptly
            this.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    if (out != null) {
                        out.println("LEAVE"); // Notify server about departure
                    }
                    System.exit(0);
                }
            });
    }
    
    /**
     * Establishes a socket connection to the server using the provided IP and username.
     * @param ip The server IP address.
     * @param username The player's chosen username.
     */
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
    
    /**
     * Helper method to show error pop-ups on the Event Dispatch Thread.
     * @param message The error message to display.
     */
    public void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }
    
    /**
     * Updates the UI list of players currently connected to the Lobby.
     * @param players A list of connected player names.
     */
    public void updateConnectedPlayers(List<String> players) {
        SwingUtilities.invokeLater(() ->
                lobbyScreen.updateConnected(players)
        );
    }
    
    /**
     * Informs the server that the player wants to join a game and switches to the waiting screen.
     */
    private void sendPlayRequest() {
        if (out != null) {
            out.println("Play:");
            cardLayout.show(mainPanel, "WAITING");
        }
    }
    
    /**
     * Updates the waiting room list and manages transitions, ensuring game flow isn't interrupted.
     * @param players List of players currently in the waiting room.
     */
    public void updateWaitingPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            waitingRoomScreen.updateWaitingPlayers(players);
            lobbyScreen.updateLobbyStatus(players, currentUsername);

            // Logic to prevent returning a player to the waiting screen if they are already in a game
            if (players.contains(currentUsername)) {
                // Only transition to WAITING if the game screen isn't already active
                if (!gameScreen.isShowing()) {
                    cardLayout.show(mainPanel, "WAITING");
                }
            }
        });
    }

    /**
     * Appends a status message to the lobby's chat/status area.
     * @param message The message to append.
     */
    public void appendStatus(String message) {
        SwingUtilities.invokeLater(() ->
                lobbyScreen.appendStatus(message)
        );
    }

    /**
     * Parses a comma-separated string of names into a List.
     * @param data The raw string data from the server.
     * @return A List of parsed names.
     */
    public List<String> parseNames(String data) {
        if (data == null || data.trim().isEmpty()) return Arrays.asList();
        return Arrays.asList(data.split(","));
    }

    /**
     * Manually switches the view to the waiting room screen.
     */
    public void showWaitingScreen() {
        cardLayout.show(mainPanel, "WAITING");
    }
    
    /**
     * Called when the server confirms a successful connection.
     * Navigates to the lobby unless the player is already engaged in a game or waiting room.
     */
    public void showSuccessAndNavigate() {
        // Prevent navigation if player is already in game or waiting room
        if (gameScreen.isShowing() || waitingRoomScreen.isShowing()) return; 

        SwingUtilities.invokeLater(() -> {
            this.connected = true;
            cardLayout.show(mainPanel, "LOBBY");
        });
    }

    
    /**
     * Switches the view back to the Lobby screen.
     */
    public void showLobby() { 
        if (gameScreen != null) {
        gameScreen.clearStatusArea();
    }
    cardLayout.show(mainPanel, "LOBBY"); }

    /**
     * Handles connection failures by displaying an error and resetting state.
     * @param errorMessage The error details to show.
     */
    public void handleConnectionError(String errorMessage) {
        this.connected = false; 
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Sends the current input word from the game screen to the server for validation.
     */
    private void sendWord() {
        String word = gameScreen.getInputWord();
        if (!word.isEmpty()) {
            out.println("SUBMIT_WORD:" + word);
        }
    }
    
    /**
     * Prompts the user for confirmation and notifies the server before closing the application.
     */
    private void leaveGame() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave?");
        if (confirm == JOptionPane.YES_OPTION) {
            out.println("LEAVE");
            System.exit(0);
        }
    }

    /**
     * @return The GameScreen instance.
     */
    public GameScreen getGameScreen() {
        return gameScreen;
    }

    /**
     * Transitions the UI to the Game screen and ensures all visual components are properly rendered.
     */
    public void showGameScreen() {
        SwingUtilities.invokeLater(() -> {
            gameScreen.clearStatusArea();
            cardLayout.show(mainPanel, "GAME");
            
            // Revalidate and repaint to ensure components (and backgrounds) appear correctly
            mainPanel.revalidate();
            mainPanel.repaint();
            
            if (gameScreen != null) {
                gameScreen.revalidate();
                gameScreen.repaint();
                // Explicitly set visible to ensure background images render correctly
                gameScreen.setVisible(true);
            }
        });
    }

    /**
     * @return The LobbyScreen instance.
     */
    public LobbyScreen getLobbyScreen() {
        return lobbyScreen;
    }

    /**
     * Sends a request to the server to leave the waiting room and return to the lobby.
     */
    public void handleLeaveWaitingRoom() {
        if (out != null) {
            out.println("LEAVE_WAITING_ROOM");
        }
    }
    

}
