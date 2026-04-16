import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * The main Server class that manages all connected players and the game logic.
 */
public class Server {
    // Lists to keep track of clients and game states
    private static ArrayList<NewClient> clients = new ArrayList<>();
    private static ArrayList<String> connectedPlayers = new ArrayList<>();
    private static ArrayList<String> waitingRoom = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Start server on port 9090
        ServerSocket serverSocket = new ServerSocket(9090);

        while (true) {
            System.out.println("Waiting for client connection...");
            Socket client = serverSocket.accept(); // Wait for a player to connect
            System.out.println("Connected to client!");

            // Create a new thread for each client to handle their requests
            NewClient clientThread = new NewClient(client, clients);
            clients.add(clientThread);
            new Thread(clientThread).start();
        }
    }

    // Adds a player to the waiting room and starts the game if the room is full.
    public static synchronized void AddToWaitingRoom(String playerName) {
        // Prevent adding more than 4 players
        if (waitingRoom.size() >= 4) {
            updateAllClients(); 
            return;
        }

        // Add player if they are not already in the room
        if (!waitingRoom.contains(playerName)) {
            waitingRoom.add(playerName);
            updateAllClients(); // Inform everyone about the new player

            // If room reaches 4 players, trigger game start
            if (waitingRoom.size() == 4) {
                startGame();
            }
        }
    }

    // Informs all clients that the game has officially started.
    private static void startGame() {
        for (NewClient client : clients) {
            client.sendMessage("GAME_START");
        }
    }
    
    public static synchronized int getWaitingCount() {
        return waitingRoom.size();
    }

    // Registers a player as 'Connected' in the Lobby.
    public static synchronized void AddConnectedPlayer(String playerName) {
        if (!connectedPlayers.contains(playerName)) {
            connectedPlayers.add(playerName);
            System.out.println("CONNECTED: " + playerName);
            updateAllClients();
        }
    }

    // Checks if a username is already being used to prevent duplicates.
    public static synchronized boolean isNameTaken(String playerName) {
        return connectedPlayers.contains(playerName);
    }

    //Broadcasts the updated lists of connected players and waiting room status to everyone.
    public static synchronized void updateAllClients() {
        // Prepare the protocols (WAITING: and PLAYERS:)
        String players = "WAITING:" + String.join(",", waitingRoom);
        String connected = "PLAYERS:" + String.join(",", connectedPlayers);

        System.out.println("Broadcasting: " + players);
        System.out.println("Broadcasting: " + connected);

        // Send the latest data to every connected client
        for (NewClient client : clients) {
            client.sendMessage(connected);
            client.sendMessage(players);
        }
    }
}
