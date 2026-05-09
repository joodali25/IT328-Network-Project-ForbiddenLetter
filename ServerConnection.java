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
 * Runs in a separate thread to keep the UI responsive and handle network traffic.
 */
public class ServerConnection implements Runnable {

    private Socket server;
    private BufferedReader in;
    private ClientGUI gui;

    /**
     * Initializes the connection to the server and sets up the input reader.
     * @param s The active socket connection.
     * @param gui Reference to the main Client GUI.
     * @throws IOException If the input stream cannot be created.
     */
    public ServerConnection(Socket s, ClientGUI gui) throws IOException {
        this.server = s;
        this.gui = gui;
        // Reader to receive messages from the server
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    /**
     * The main execution loop for the thread. 
     * Continues to read lines from the server until the connection is closed.
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
     * Flag used to prevent multiple Game Over dialogs from appearing simultaneously.
     */
    private boolean isGameOverWindowShowing = false;

    /**
     * Parses the incoming server message and triggers the corresponding GUI action.
     * @param message The raw protocol string received from the server.
     */
    private void handleMessage(String message) {
        
        // Protocol: Handle player list updates for the Lobby
        if (message.startsWith("PLAYERS:")) {
            List<String> players = gui.parseNames(message.substring(8));
            SwingUtilities.invokeLater(() -> {
                gui.updateConnectedPlayers(players);
                gui.showSuccessAndNavigate(); 
            });
        }
        // Protocol: Handle waiting room updates
        else if (message.startsWith("WAITING:")) {
            List<String> players = gui.parseNames(message.substring(8));
            SwingUtilities.invokeLater(() -> gui.updateWaitingPlayers(players));
        }
        // Protocol: Handle general information messages to be displayed in game chat
        else if (message.startsWith("INFO:")) {
            String infoMsg = message.substring(5); // Remove "INFO:" prefix
            SwingUtilities.invokeLater(() -> {
                if (gui.getGameScreen() != null) {
                    gui.getGameScreen().appendStatus(infoMsg);
                }
            });
        }
        
        // Protocol: Update winner info in the status area (without pop-up)
        else if (message.startsWith("WINNER:")) {
            String name = message.substring(7);
            SwingUtilities.invokeLater(() -> {
                if (gui.getGameScreen() != null) {
                    gui.getGameScreen().appendStatus("🏆 Winner is: " + name);
                }
            });
        }

        // Protocol: Display detailed winner leaderboard using a custom dialog
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

        // Protocol: Handle cases where the game ends without a specific winner
        else if (message.startsWith("NO_WINNER:")) {
            if (isGameOverWindowShowing) return;
            isGameOverWindowShowing = true;
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gui, message.substring(10), "Ended", JOptionPane.INFORMATION_MESSAGE);
                isGameOverWindowShowing = false;
                gui.showLobby();
            });
        }

        // Protocol: Update scores displayed in the GameScreen
        else if (message.startsWith("SCORES:")) {
             String scores = message.substring(7);
             SwingUtilities.invokeLater(() -> {
                   if (gui.getGameScreen() != null) gui.getGameScreen().updateScore(scores);
             });
        }
        // Protocol: Transition to the GameScreen and force repaint
        else if (message.equals("GAME_START")) {
            isGameOverWindowShowing = false;
            SwingUtilities.invokeLater(() -> {
                gui.showGameScreen();
                if (gui.getGameScreen() != null) {
                    gui.getGameScreen().repaint();
                }
            });
        }
        // Protocol: Update current level info (topic, forbidden letter, etc.)
        else if (message.startsWith("LEVEL_RULES:")) {
            String[] p = message.split(":");
            SwingUtilities.invokeLater(() -> {
                gui.showGameScreen(); 
                gui.getGameScreen().updateGameInfo(p[1], p[2], p[3], p[5]);
                gui.getGameScreen().revalidate();
                gui.getGameScreen().repaint();
            });
        }
        // Protocol: Update the current round timer
        else if (message.startsWith("TIMER_UPDATE:")) {
            SwingUtilities.invokeLater(() -> gui.getGameScreen().updateTimer(message.substring(13)));
        }
        // Protocol: Append word validation feedback to the chat
        else if (message.startsWith("VALID_WORD:") || message.startsWith("INVALID_WORD:")) {
            SwingUtilities.invokeLater(() -> gui.getGameScreen().appendStatus(message));
        }
        // Protocol: Disable the lobby play button during an active game
        else if (message.equals("DISABLE_PLAY")) {
            SwingUtilities.invokeLater(() -> gui.getLobbyScreen().setPlayButtonEnabled(false));
        } 
        // Protocol: Enable the lobby play button
        else if (message.equals("ENABLE_PLAY")) {
            SwingUtilities.invokeLater(() -> gui.getLobbyScreen().setPlayButtonEnabled(true));
        }
        // Protocol: Update the master session timer
        else if (message.startsWith("TOTAL_TIMER_UPDATE:")) {
            if (gui.getGameScreen() != null) {
                SwingUtilities.invokeLater(() -> gui.getGameScreen().updateTotalTimer(message.substring(19)));
            }
        }
        // Protocol: Confirm successful exit from the waiting room
        else if (message.equals("LEFT_WAITING_ROOM_SUCCESS")) {
            SwingUtilities.invokeLater(() -> gui.showLobby());
        }
    }

    /**
     * Displays a customized, styled dialog for showing final game results.
     * @param data The results data string formatted for parsing.
     */
    private void showCustomResultsDialog(String data) {
        JDialog resultsDialog = new JDialog(gui, "Game Results", true);
        resultsDialog.setLayout(new BorderLayout());
        resultsDialog.setSize(450, 550);
        resultsDialog.setLocationRelativeTo(gui);

        // Styling constants inspired by the GameScreen aesthetic
        Color colorBg = new Color(255, 248, 235);
        Color colorAccent = new Color(110, 145, 105); // Success/Winner color
        Color colorText = new Color(55, 40, 25);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(colorBg);
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Parse and process leaderboard data
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

        // Results leaderboard panel
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
            // Highlight the overall winner
            if(parts[0].equals(topWinner)) pLabel.setForeground(new Color(180, 140, 50));
            scorePanel.add(pLabel);
        }

        // Close button to navigate back to Lobby
        JButton closeBtn = new JButton("Back to Lobby");
        closeBtn.setBackground(colorText);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setFont(new Font("Serif", Font.BOLD, 18));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> resultsDialog.dispose());

        // Assemble components into the main container
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
}
