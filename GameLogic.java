import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The main Server class that manages all connected players and the game logic.
 * It handles client connections, waiting rooms, timers, and game progression.
 */
public class Server {
    public static GameLogic gameLogic = new GameLogic();
    private static java.util.Timer waitingTimer;
    private static java.util.Timer gameTimer;
    private static java.util.Timer levelTimer;
    
    // Lists to keep track of clients and game states
    private static ArrayList<NewClient> clients = new ArrayList<>();
    private static ArrayList<String> connectedPlayers = new ArrayList<>();
    private static ArrayList<String> waitingRoom = new ArrayList<>();

    /**
     * Entry point for the server application.
     * Initializes the ServerSocket and listens for incoming client connections.
     */
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

    /**
     * Adds a player to the waiting room. 
     * Starts the waiting timer if 2 players join, or starts the game if 4 players are reached.
     * @param playerName The name of the player joining the room.
     */
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

    /**
     * Starts a 30-second countdown once the minimum number of players (2) is met.
     */
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
        }, 30000); // 30 seconds delay
    }

    /**
     * Transitions from waiting phase to active gameplay.
     * Cancels the waiting timer and broadcasts initial game signals to participants.
     */
    private static void startGame() {
        if (waitingTimer != null) {
            waitingTimer.cancel();
            waitingTimer = null;
        }

        gameLogic.startGame(waitingRoom); // start 
        updateAllClients();
        
        // Broadcast game start and level data only to participants
        broadcastToGame("GAME_START"); 
        broadcastToGame(gameLogic.getCurrentLevelMessage());
        broadcastToGame(gameLogic.getScoresMessage());
        
        startGameTimer();
        startLevelTimerIfNeeded();
    }

    /**
     * Manages a countdown timer for the current level.
     * Handles level transitions and notifies clients of time updates.
     */
    private static void startLevelTimerIfNeeded() {
        if (levelTimer != null) {
            levelTimer.cancel();
            levelTimer = null;
        }

        int seconds = gameLogic.getCurrentLevel().getLevelTimeSeconds();

        if (seconds == 0) return;

        levelTimer = new java.util.Timer();
        // Array to hold the remaining time inside the timer task
        final int[] timeLeft = {seconds}; 

        levelTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                synchronized (Server.class) {
                    if (!gameLogic.isGameRunning()) {
                        this.cancel();
                        return;
                    }

                    if (timeLeft[0] <= 0) {
                        this.cancel();
                        boolean hasNext = gameLogic.moveToNextLevel();
                        if (hasNext) {
                            broadcastToGame("LEVEL_TIMEOUT");
                            broadcastToGame(gameLogic.getCurrentLevelMessage());
                            startLevelTimerIfNeeded();
                        } else {
                            gameLogic.endGame();
                            broadcastToGame("NO_WINNER:Time's up! No player reached target score.");
                        }
                    } else {
                        // Send countdown updates to the UI
                        broadcastToGame("TIMER_UPDATE:" + timeLeft[0]);
                        timeLeft[0]--;
                    }
                }
            }
        }, 0, 1000); // Update every 1000ms
    }

    /**
     * Manages the overall game session timer, fixed at 7 minutes.
     */
    private static void startGameTimer() {
        if (gameTimer != null) gameTimer.cancel();
        gameTimer = new java.util.Timer();
        
        final int[] totalSeconds = {7 * 60}; // 420 seconds total

        gameTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (totalSeconds[0] <= 0) {
                    stopGame("NO_WINNER:Game time ended (7 minutes).");
                    this.cancel();
                } else {
                    int mins = totalSeconds[0] / 60;
                    int secs = totalSeconds[0] % 60;
                    String timeStr = String.format("%02d:%02d", mins, secs);
                    
                    // Broadcast session timer updates to players
                    broadcastToGame("TOTAL_TIMER_UPDATE:" + timeStr);
                    totalSeconds[0]--;
                }
            }
        }, 0, 1000);
    }

    /**
     * Processes word submissions from players.
     * Updates scores, checks for winners, and handles level progression.
     * @param playerName Name of the player submitting.
     * @param word The word string.
     * @param sender The client handler thread.
     */
    public static synchronized void submitWord(String playerName, String word, NewClient sender) {
        ValidationResult result = gameLogic.submitWord(playerName, word);
        sender.sendMessage(result.getMessage());

        if (!result.isValid()) return;

        broadcastToGame(gameLogic.getScoresMessage());
        
        // 1. Check if the player reached the target score
        if (gameLogic.hasWinner(playerName)) {
            // Stop the game and notify everyone
            stopGame("WINNER:" + playerName); 
            return;
        }

        boolean hasNext = gameLogic.moveToNextLevel();

        if (hasNext) {
            broadcastToGame(gameLogic.getCurrentLevelMessage());
            startLevelTimerIfNeeded();
        } else {
            // 2. All levels completed (find the highest scorer)
            gameLogic.endGame();
            if (gameTimer != null) gameTimer.cancel();
            if (levelTimer != null) levelTimer.cancel();

            String winnerList = gameLogic.getWinnerListMessage();
            // Extract the first player (top scorer)
            String topEntry = winnerList.substring(12).split(",")[0]; 
            String topPlayer = topEntry.split("=")[0];

            stopGame(gameLogic.getFinalResultMessage());
        }
    }

    /**
     * Removes a player from the game session and informs other participants.
     * Ends the game if only one player remains.
     * @param playerName Name of the player to remove.
     */
    public static synchronized void removePlayerFromGame(String playerName) {
        // 1. Send departure info while player is still tracked to ensure message delivery
        if (gameLogic.isGameRunning()) {
            broadcastToGame("INFO: " + playerName + " has left the game.");
        }

        // 2. Perform cleanup and removal
        connectedPlayers.remove(playerName);
        waitingRoom.remove(playerName);
        gameLogic.removePlayer(playerName);

        // 3. Manage timers and termination logic
        if (waitingRoom.size() < 2) {
            if (waitingTimer != null) {
                waitingTimer.cancel();
                waitingTimer = null;
            }
        }

        if (gameLogic.isGameRunning() && gameLogic.getActivePlayerCount() <= 1) {
            stopGame("NO_WINNER:Only one player left. Game ended.");
        }

        updateAllClients();
    }

    /**
     * Sends a message only to players currently inside the game room.
     * @param message The message to broadcast.
     */
    private static void broadcastToGame(String message) {
        for (NewClient client : clients) {
            if (waitingRoom.contains(client.getPlayerName())) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Sends a message to every connected client on the server.
     * @param message The message to broadcast.
     */
    private static void broadcast(String message) {
        for (NewClient client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * @return The number of players currently in the waiting room.
     */
    public static synchronized int getWaitingCount() {
        return waitingRoom.size();
    }

    /**
     * Registers a player as 'Connected' in the global lobby.
     * @param playerName The name to add.
     */
    public static synchronized void AddConnectedPlayer(String playerName) {
        if (!connectedPlayers.contains(playerName)) {
            connectedPlayers.add(playerName);
            System.out.println("CONNECTED: " + playerName);
            updateAllClients();
        }
    }

    /**
     * Checks if a username is already being used.
     * @param playerName The name to check.
     * @return true if taken, false otherwise.
     */
    public static synchronized boolean isNameTaken(String playerName) {
        return connectedPlayers.contains(playerName);
    }

    /**
     * Broadcasts the updated player lists and lobby play-button status to everyone.
     */
    public static synchronized void updateAllClients() {
        String players = "WAITING:" + String.join(",", waitingRoom);
        String connected = "PLAYERS:" + String.join(",", connectedPlayers);
        
        // Disable play button if game is running or room is at max capacity
        boolean shouldDisable = gameLogic.isGameRunning() || waitingRoom.size() >= 4;
        String playStatus = shouldDisable ? "DISABLE_PLAY" : "ENABLE_PLAY";

        for (NewClient client : clients) {
            client.sendMessage(connected);
            client.sendMessage(players);
            client.sendMessage(playStatus); 
        }
    }

    /**
     * Terminates the game session, broadcasts results, and clears the waiting room.
     * @param message The termination reason or winner announcement.
     */
    public static synchronized void stopGame(String message) {
        // 1. Send results first while players are still in the room
        broadcast(gameLogic.getWinnerListMessage());
        broadcast(message); 

        // 2. Perform stop and cleanup
        gameLogic.endGame(); 
        waitingRoom.clear(); 
        updateAllClients();
        
        System.out.println("Game stopped and results sent.");
    }

    /**
     * Removes a player specifically from the waiting room list.
     * @param playerName Name of the player leaving.
     */
    public static synchronized void removeFromWaitingRoom(String playerName) {
        waitingRoom.remove(playerName);
        System.out.println(playerName + " left the waiting room.");
    }
    
} // class end
