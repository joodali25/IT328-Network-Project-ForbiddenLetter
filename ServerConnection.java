import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import javax.swing.border.CompoundBorder;

/**
 * Listens for messages from the server and updates the Client GUI.
 * Runs in a separate thread to keep the UI responsive.
 */
public class ServerConnection implements Runnable {

    private Socket server;
    private BufferedReader in;
    private ClientGUI gui;

    /**
     * Constructor to initialize the server connection and the GUI reference.
     * @param s The socket connected to the server.
     * @param gui The main Client GUI instance.
     * @throws IOException If an input stream cannot be created.
     */
    public ServerConnection(Socket s, ClientGUI gui) throws IOException {
        this.server = s;
        this.gui = gui;
        // Reader to receive messages from the server
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    /**
     * The main execution loop for the thread.
     * Continuously reads messages from the server as long as the connection is open.
     */
    @Override
    public void run() {
        try {
            String msg;
            // Keep listening as long as the connection is open
            while ((msg = in.readLine()) != null) {
                handleMessage(msg);
            }
        } catch (IOException ex) {
            gui.appendStatus("Connection error: " + ex.getMessage());
        }
    }

    /**
     * Flag to prevent duplicate Game Over windows from appearing simultaneously.
     */
    private boolean isGameOverWindowShowing = false;

    /**
     * Parses the incoming server message and triggers the corresponding GUI action.
     * @param message The raw string message received from the server.
     */
    private void handleMessage(String message) {
        
        // Handles player list updates for the initial connection
        if (message.startsWith("PLAYERS:")) {
            List<String> players = gui.parseNames(message.substring(8));
            SwingUtilities.invokeLater(() -> {
                gui.updateConnectedPlayers(players);
                gui.showSuccessAndNavigate(); 
            });
        }
        // Updates the waiting room player list
        else if (message.startsWith("WAITING:")) {
            List<String> players = gui.parseNames(message.substring(8));
            SwingUtilities.invokeLater(() -> gui.updateWaitingPlayers(players));
        }
        
        // 1. Ignore WINNER message for dialogs (only log it in the chat status)
        else if (message.startsWith("WINNER:")) {
            String name = message.substring(7);
            SwingUtilities.invokeLater(() -> {
                if (gui.getGameScreen() != null) {
                    gui.getGameScreen().appendStatus("🏆 Winner is: " + name);
                }
            });
        }

        // 2. Process WINNER_LIST and use a flag lock to prevent multiple windows
        else if (message.startsWith("WINNER_LIST:")) {
            if (isGameOverWindowShowing) return;
            isGameOverWindowShowing = true;
            String data = message.substring(12);
            
            SwingUtilities.invokeLater(() -> {
                showCustomResultsDialog(data);
                isGameOverWindowShowing = false;
                gui.showLobby();
            });
        }

        // Handles cases where the game ends without a winner
        else if (message.startsWith("NO_WINNER:")) {
            if (isGameOverWindowShowing) return;
            isGameOverWindowShowing = true;
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gui, message.substring(10), "Ended", JOptionPane.INFORMATION_MESSAGE);
                isGameOverWindowShowing = false;
                gui.showLobby();
            });
        }

        // Updates player scores in the game UI
        else if (message.startsWith("SCORES:")) {
             String scores = message.substring(7);
             SwingUtilities.invokeLater(() -> {
                   if (gui.getGameScreen() != null) gui.getGameScreen().updateScore(scores);
             });
        }
        // Signals the start of the game and switches the view
        else if (message.equals("GAME_START")) {
            isGameOverWindowShowing = false;
            SwingUtilities.invokeLater(() -> {
                gui.showGameScreen();
                // Force the panel to repaint immediately on start
                if (gui.getGameScreen() != null) {
                    gui.getGameScreen().repaint();
                }
            });
        }
        // Receives and applies specific level rules and constraints
        else if (message.startsWith("LEVEL_RULES:")) {
            String[] p = message.split(":");
            SwingUtilities.invokeLater(() -> {
                // 1. Show the screen first
                gui.showGameScreen(); 
                
                // 2. Update data
                gui.getGameScreen().updateGameInfo(p[1], p[2], p[3], p[5]);
                
                // 3. Request full revalidation and repaint of background and components
                gui.getGameScreen().revalidate();
                gui.getGameScreen().repaint();
            });
        }
        // Updates the remaining time for the current round
        else if (message.startsWith("TIMER_UPDATE:")) {
            SwingUtilities.invokeLater(() -> gui.getGameScreen().updateTimer(message.substring(13)));
        }
        // Displays validation results of submitted words
        else if (message.startsWith("VALID_WORD:") || message.startsWith("INVALID_WORD:")) {
            SwingUtilities.invokeLater(() -> gui.getGameScreen().appendStatus(message));
        }
        // Disables the play button if the room is full or game is in progress
        else if (message.equals("DISABLE_PLAY")) {
            SwingUtilities.invokeLater(() -> gui.getLobbyScreen().setPlayButtonEnabled(false));
        } 
        // Enables the play button when the lobby is ready
        else if (message.equals("ENABLE_PLAY")) {
            SwingUtilities.invokeLater(() -> gui.getLobbyScreen().setPlayButtonEnabled(true));
        }
        // Updates the master game timer
        else if (message.startsWith("TOTAL_TIMER_UPDATE:")) {
            if (gui.getGameScreen() != null) {
                SwingUtilities.invokeLater(() -> gui.getGameScreen().updateTotalTimer(message.substring(19)));
            }
        }
        // Handles successful exit from the waiting room back to the lobby
        else if (message.equals("LEFT_WAITING_ROOM_SUCCESS")) {
            SwingUtilities.invokeLater(() -> gui.showLobby());
        }
    }

    /**
     * Displays a custom, styled dialog showing the final game results and leaderboard.
     * @param data The score data string formatted as "Player=Score,..."
     */
    private void showCustomResultsDialog(String data) {
        JDialog resultsDialog = new JDialog(gui, "Game Results", true);
        resultsDialog.setLayout(new BorderLayout());
        resultsDialog.setSize(450, 550);
        resultsDialog.setLocationRelativeTo(gui);

        // Colors inspired by GameScreen aesthetics
        Color colorBg = new Color(255, 248, 235);
        Color colorAccent = new Color(110, 145, 105); // Success/Win color
        Color colorText = new Color(55, 40, 25);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(colorBg);
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Parse the scores data
        String[] entries = data.split(",");
        String topWinner = entries[0].split("=")[0];

        // Winner icon/header
        JLabel winnerIcon = new JLabel("🏆", SwingConstants.CENTER);
        winnerIcon.setFont(new Font("Serif", Font.PLAIN, 60));
        winnerIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerLabel = new JLabel(topWinner + " Wins!", SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Serif", Font.BOLD, 32));
        winnerLabel.setForeground(colorAccent);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Leaderboard panel
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(0, 1, 10, 10));
        scorePanel.setBackground(colorBg);
        scorePanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(85, 60, 35), 2),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel scoreTitle = new JLabel("Final Leaderboard:");
        scoreTitle.setFont(new Font("Serif", Font.ITALIC, 18));
        scoreTitle.setForeground(colorText);
        scorePanel.add(scoreTitle);

        for (String entry : entries) {
            String[] parts = entry.split("=");
            JLabel pLabel = new JLabel(parts[0] + " ➔ " + parts[1] + " Points");
            pLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            pLabel.setForeground(colorText);
            // Highlight the top winner with a different color
            if(parts[0].equals(topWinner)) pLabel.setForeground(new Color(180, 140, 50));
            scorePanel.add(pLabel);
        }

        // Close button to return to lobby
        JButton closeBtn = new JButton("Back to Lobby");
        closeBtn.setBackground(colorText);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setFont(new Font("Serif", Font.BOLD, 18));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> resultsDialog.dispose());

        // Assemble components
        mainContent.add(winnerIcon);
        mainContent.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContent.add(winnerLabel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 30)));
        mainContent.add(scorePanel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 30)));
        mainContent.add(closeBtn);

        resultsDialog.add(mainContent);
        resultsDialog.setVisible(true);
    }
} // class end
