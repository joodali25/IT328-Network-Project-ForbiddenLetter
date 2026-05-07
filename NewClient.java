import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Handles communication for a single client on the server side.
 * This class runs in a separate thread to manage multiple players.
 */
class NewClient implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private boolean isConnected;
    private ArrayList<NewClient> clients;

    public NewClient(Socket c, ArrayList<NewClient> clients) throws IOException {
        this.client = c;
        this.clients = clients;

        // Setting up Input/Output streams for network communication
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            while (true) {

                // Read the request sent by the client
                String request = in.readLine();

                if (request == null) break; // Exit loop if client disconnects

                // Protocol 1: Handle initial connection
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

                // Protocol 2: Handle player clicking the 'PLAY' button
                else if (request.startsWith("Play:")) {

                    if (playerName != null) {
                        Server.AddToWaitingRoom(playerName);
                    }
                }

                // Protocol 3: Handle word submission during the game
                else if (request.startsWith("SUBMIT_WORD:")) {

                    String word = request.substring(12).trim();

                    if (playerName != null) {
                        Server.submitWord(playerName, word, this);

                    } else {
                        sendMessage("INVALID_WORD:Player is not connected");
                    }
                }

                // Protocol 4: Handle player leaving the game manually
                else if (request.equals("LEAVE")) {

                    if (playerName != null) {
                        Server.removePlayerFromGame(playerName);
                    }

                    break;
                }
            }

        } catch (IOException e) {

            System.out.println("Client disconnected unexpectedly");

        } finally {

            // Remove player from the game if connection is lost
            if (playerName != null) {
                Server.removePlayerFromGame(playerName);
            }

            closeResources();
        }
    }

    // Closes the connection and releases system resources.
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

    // Sends a direct message to this specific client.
    public void sendMessage(String message) {
        out.println(message);
    }

    // Returns the current player's username
    public String getPlayerName() {
        return playerName;
    }
}
