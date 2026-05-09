import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication for a single client on the server side.
 * This class runs in a separate thread to manage multiple players simultaneously.
 */
class NewClient implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private boolean isConnected;
    private ArrayList<NewClient> clients;

    /**
     * Initializes the client handler with the socket and the shared client list.
     * @param c The client socket connection.
     * @param clients The list of all active client threads.
     * @throws IOException If streams cannot be established.
     */
    public NewClient(Socket c, ArrayList<NewClient> clients) throws IOException {
        this.client = c;
        this.clients = clients;

        // Setting up Input/Output streams for network communication
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    /**
     * The main execution loop that listens for and processes client requests.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Read the request sent by the client
                String request = in.readLine();

                if (request == null) break; // Exit loop if client disconnects

                // Protocol 1: Handle initial connection and identity registration
                if (request.startsWith("CONNECT:")) {
                    String newName = request.substring(8).trim();

                    // Check if the username is already taken in the Server
                    if (Server.isNameTaken(newName)) {
                        sendMessage("ERROR:Name already taken");
                    } else {
                        this.playerName = newName;
                        this.isConnected = true;
                        Server.AddConnectedPlayer(playerName);
                    }
                }
                
                // Protocol: Handle request to leave the waiting room
                if (request.equals("LEAVE_WAITING_ROOM")) {
                    Server.removeFromWaitingRoom(playerName); // Remove player from the waiting list
                    Server.updateAllClients(); // Notify everyone of the updated list
                    sendMessage("LEFT_WAITING_ROOM_SUCCESS"); // Confirm successful exit to the client
                    continue; 
                }

                // Protocol 2: Handle player clicking the 'PLAY' button
                else if (request.startsWith("Play:")) {
                    if (playerName != null) {
                        Server.AddToWaitingRoom(playerName);
                    }
                }
                
                // Protocol Extension: Redundant check for leaving the waiting room or game session
                else if (request.equals("LEAVE_WAITING_ROOM")) {
                    if (playerName != null) {
                        Server.removePlayerFromGame(playerName);
                    }
                }
                

                // Protocol 3: Handle word submission during active gameplay
                else if (request.startsWith("SUBMIT_WORD:")) {
                    String word = request.substring(12).trim();

                    if (playerName != null) {
                        Server.submitWord(playerName, word, this);
                        
                        // Verification: Check if this submission triggered a win and terminate session if so
                        if (Server.gameLogic.hasWinner(playerName)) {
                            Server.stopGame("WINNER:" + playerName);
                        }
                    } else {
                        sendMessage("INVALID_WORD:Player is not connected");
                    }
                }

                // Protocol 4: Handle player leaving the game manually via GUI actions
                else if (request.equals("LEAVE")) {
                    if (playerName != null) {
                        Server.removePlayerFromGame(playerName);
                        
                        // Logic update: End the game session if participants drop below the minimum required (2)
                        if (Server.gameLogic.isGameRunning() && Server.getWaitingCount() < 2) {
                            Server.stopGame("NO_WINNER:Not enough players to continue.");
                        }
                    }
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected unexpectedly: " + playerName);
        } finally {
            // Cleanup: Remove player from game logic if connection is lost
            if (playerName != null) {
                Server.removePlayerFromGame(playerName);
                
                // Logic update: Ensure game closure and list cleanup on sudden disconnects
                if (Server.gameLogic.isGameRunning() && Server.getWaitingCount() < 2) {
                    Server.stopGame("NO_WINNER:Connection lost. Game ended.");
                }
            }
            closeResources();
        }
    }

    /**
     * Closes the network connection and releases system resources associated with this client.
     */
    private void closeResources() {

        try {

            if (out != null) out.close();
            if (in != null) in.close();
            if (client != null) client.close();

            System.out.println("Resources closed for: " + playerName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a direct string message to this specific client instance.
     * @param message The message to be sent via the output stream.
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * @return The registered username of the player handled by this thread.
     */
    public String getPlayerName() {
        return playerName;
    }
}
