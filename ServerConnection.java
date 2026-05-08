import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

/*
 * Listens for messages from the server and updates the Client GUI.
 * Runs in a separate thread to keep the UI responsive.
 */
public class ServerConnection implements Runnable {

    private Socket server;
    private BufferedReader in;
    private ClientGUI gui;

    public ServerConnection(Socket s, ClientGUI gui) throws IOException {
        this.server = s;
        this.gui = gui;
        // Reader to receive messages from the server
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

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
     * Parses the server message and triggers the correct GUI action.
     */
    private void handleMessage(String message) {
        
        // Protocol: Update the list of all connected players in the Lobby
        if (message.startsWith("PLAYERS:")) {
            List<String> players = gui.parseNames(message.substring(8));

            SwingUtilities.invokeLater(() -> {
                gui.updateConnectedPlayers(players);
                
                // Once connected and validated, move player to the LOBBY screen
                gui.showSuccessAndNavigate(); 
            });
        }

        // Protocol: Update the waiting room list
        else if (message.startsWith("WAITING:")) {
            List<String> players = gui.parseNames(message.substring(8));

            SwingUtilities.invokeLater(() ->
                    gui.updateWaitingPlayers(players)
            );
        }

        // Protocol: Handle errors (e.g., Name already taken)
        else if (message.startsWith("ERROR:")) {
            SwingUtilities.invokeLater(() ->
                    gui.handleConnectionError(message.substring(6))
            );
        }
        else if (message.startsWith("SCORES:")) {
             SwingUtilities.invokeLater(() ->
                   gui.appendStatus("Scores updated: " + message.substring(7))
            );
        }

       else if (message.startsWith("WINNER:")) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null,
                    "Winner is: " + message.substring(7),
                    "Game Ended",
                    JOptionPane.INFORMATION_MESSAGE)
             );
        }

      else if (message.startsWith("WINNER_LIST:")) {
           SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null,
                    "Final Scores:\n" + message.substring(12).replace(",", "\n"),
                    "Winner List",
                    JOptionPane.INFORMATION_MESSAGE)
             );
         }

      else if (message.startsWith("NO_WINNER:")) {
           SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null,
                    message.substring(10),
                    "No Winner",
                    JOptionPane.INFORMATION_MESSAGE)
             );
         }
        
    }//method end
}//class end
