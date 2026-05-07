import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
private static GameLogic gameLogic = new GameLogic();
private static java.util.Timer waitingTimer;
private static java.util.Timer gameTimer;
private static java.util.Timer levelTimer;

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
    if (waitingRoom.size() >= 4) {
        updateAllClients();
        return;
    }

    if (!waitingRoom.contains(playerName)) {
        waitingRoom.add(playerName);
        updateAllClients();

        if (waitingRoom.size() == 2) {
            startWaitingTimer();
        }

        if (waitingRoom.size() == 4) {
            startGame();
        }
    }
}

    private static void startWaitingTimer() {
    if (waitingTimer != null) return;

    waitingTimer = new java.util.Timer();
    waitingTimer.schedule(new java.util.TimerTask() {
        @Override
        public void run() {
            synchronized (Server.class) {
                if (waitingRoom.size() >= 2 && !gameLogic.isGameRunning()) {
                    startGame();
                }
            }
        }
    }, 30000);
}

private static void startGame() {
    if (waitingTimer != null) {
        waitingTimer.cancel();
        waitingTimer = null;
    }

    gameLogic.startGame(waitingRoom);

    broadcast("GAME_START");
    broadcast(gameLogic.getCurrentLevelMessage());
    broadcast(gameLogic.getScoresMessage());

    startGameTimer();
    startLevelTimerIfNeeded();
}

private static void startGameTimer() {
    if (gameTimer != null) gameTimer.cancel();

    gameTimer = new java.util.Timer();
    gameTimer.schedule(new java.util.TimerTask() {
        @Override
        public void run() {
            synchronized (Server.class) {
                if (gameLogic.isGameRunning()) {
                    gameLogic.endGame();
                    if (levelTimer != null) levelTimer.cancel();
                    broadcast("NO_WINNER:Game time ended");
                }
            }
        }
    }, 7 * 60 * 1000);
}

private static void startLevelTimerIfNeeded() {
    if (levelTimer != null) {
        levelTimer.cancel();
        levelTimer = null;
    }

    int seconds = gameLogic.getCurrentLevel().getLevelTimeSeconds();

    if (seconds <= 0) return;

    levelTimer = new java.util.Timer();
    levelTimer.schedule(new java.util.TimerTask() {
        @Override
        public void run() {
            synchronized (Server.class) {
                if (!gameLogic.isGameRunning()) return;

                boolean hasNext = gameLogic.moveToNextLevel();

                if (hasNext) {
                    broadcast("LEVEL_TIMEOUT");
                    broadcast(gameLogic.getCurrentLevelMessage());
                    startLevelTimerIfNeeded();
                } else {
                    gameLogic.endGame();
                    broadcast("NO_WINNER:No player reached 5 points");
                }
            }
        }
    }, seconds * 1000);
}

public static synchronized void submitWord(String playerName, String word, NewClient sender) {
    ValidationResult result = gameLogic.submitWord(playerName, word);
    sender.sendMessage(result.getMessage());

    if (!result.isValid()) return;

    broadcast(gameLogic.getScoresMessage());

    if (gameLogic.hasWinner(playerName)) {
        gameLogic.endGame();

        if (gameTimer != null) gameTimer.cancel();
        if (levelTimer != null) levelTimer.cancel();

        broadcast("WINNER:" + playerName);
        broadcast("WINNER_LIST:" + playerName);
        return;
    }

    boolean hasNext = gameLogic.moveToNextLevel();

    if (hasNext) {
        broadcast(gameLogic.getCurrentLevelMessage());
        startLevelTimerIfNeeded();
    } else {
        gameLogic.endGame();
        broadcast("NO_WINNER:No player reached 5 points");
    }
}

public static synchronized void removePlayerFromGame(String playerName) {
    connectedPlayers.remove(playerName);
    waitingRoom.remove(playerName);
    gameLogic.removePlayer(playerName);

    broadcast("PLAYER_LEFT:" + playerName);
    broadcast(gameLogic.getScoresMessage());
    updateAllClients();

    if (gameLogic.isGameRunning() && gameLogic.getActivePlayerCount() <= 1) {
        gameLogic.endGame();

        if (gameTimer != null) gameTimer.cancel();
        if (levelTimer != null) levelTimer.cancel();

        broadcast("NO_WINNER:Only one player left");
    }
}

private static void broadcast(String message) {
    for (NewClient client : clients) {
        client.sendMessage(message);
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
