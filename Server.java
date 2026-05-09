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
     * Starts the waiting timer if 2 players are present, or starts the game if 4 players are reached.
     * @param playerName The name of the player attempting to join the waiting room.
     */
    public static synchronized void AddToWaitingRoom(String playerName) {
        if (waitingRoom.size() >= 4) {
            updateAllClients();
            return;
        }

        if (!waitingRoom.contains(playerName)) {
            waitingRoom.add(playerName);
            updateAllClients();

            // Start countdown when the minimum required players (2) join
            if (waitingRoom.size() == 2) {
                startWaitingTimer();
            }

            // Immediately start game when max capacity (4) is reached
            if (waitingRoom.size() == 4) {
                startGame();
            }
        }
    }

    /**
     * Initializes a 30-second timer that starts the game if at least 2 players are in the room.
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
     * Transitions from waiting state to active game state.
     * Cancels waiting timers and broadcasts initial game data to participants.
     */
    private static void startGame() {
        if (waitingTimer != null) {
            waitingTimer.cancel();
            waitingTimer = null;
        }

        gameLogic.startGame(waitingRoom);
        updateAllClients();
        
        // Broadcast game initialization signals to participants only
        broadcastToGame("GAME_START"); 
        broadcastToGame(gameLogic.getCurrentLevelMessage());
        broadcastToGame(gameLogic.getScoresMessage());
        
        startGameTimer();
        startLevelTimerIfNeeded();
    }

    /**
     * Manages the specific countdown timer for the current game level.
     * Automatically handles level timeouts and transitions to the next level or ends the game.
     */
    private static void startLevelTimerIfNeeded() {
        if (levelTimer != null) {
            levelTimer.cancel();
            levelTimer = null;
        }

        int seconds = gameLogic.getCurrentLevel().getLevelTimeSeconds();

        if (seconds <= 0) return;

        levelTimer = new java.util.Timer();
        // Final array used to store the remaining time within the timer task
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
                        // Send second-by-second countdown updates to the UI
                        broadcastToGame("TIMER_UPDATE:" + timeLeft[0]);
                        timeLeft[0]--;
                    }
                }
            }
        }, 0, 1000); // Update every 1000ms (1 second)
    }

    /**
     * Manages the overall game session timer (fixed at 7 minutes).
     * Ends the game if the total time expires.
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
                    
                    // Broadcast total game time remaining to participants
                    broadcastToGame("TOTAL_TIMER_UPDATE:" + timeStr);
                    totalSeconds[0]--;
                }
            }
        }, 0, 1000);
    }

    /**
     * Handles word submissions from players. 
     * Validates words, updates scores, and checks for winning conditions or level progression.
     * @param playerName The name of the player submitting the word.
     * @param word The word string being submitted.
     * @param sender The specific client thread that sent the request.
     */
    public static synchronized void submitWord(String playerName, String word, NewClient sender) {
        ValidationResult result = gameLogic.submitWord(playerName, word);
        sender.sendMessage(result.getMessage());

        if (!result.isValid()) return;

        broadcastToGame(gameLogic.getScoresMessage());
        
        // 1. Check for a definitive winner (reached target score)
        if (gameLogic.hasWinner(playerName)) {
            // stopGame will handle notifying all clients and cleaning up state
            stopGame("WINNER:" + playerName); 
            return;
        }

        boolean hasNext = gameLogic.moveToNextLevel();

        if (hasNext) {
            broadcastToGame(gameLogic.getCurrentLevelMessage());
            startLevelTimerIfNeeded();
        } else {
            // 2. Handle end of all levels (determine winner by highest score)
            gameLogic.endGame();
            if (gameTimer != null) gameTimer.cancel();
            if (levelTimer != null) levelTimer.cancel();

            String winnerList = gameLogic.getWinnerListMessage();
            // Extract the first player's name (highest score) from the winner list
            String topEntry = winnerList.substring(12).split(",")[0]; 
            String topPlayer = topEntry.split("=")[0];

            // Notify everyone of the top player once game ends
            stopGame("WINNER:" + topPlayer);
        }
    }

    /**
     * Removes a player from all tracking lists and handles game termination if too few players remain.
     * @param playerName The name of the player to be removed.
     */
    public static synchronized void removePlayerFromGame(String playerName) {
        connectedPlayers.remove(playerName);
        waitingRoom.remove(playerName);
        gameLogic.removePlayer(playerName);

        // Stop the waiting timer if the room drops below the minimum required players
        if (waitingRoom.size() < 2) {
            if (waitingTimer != null) {
                waitingTimer.cancel();
                waitingTimer = null;
                System.out.println("Waiting Timer stopped: Not enough players.");
            }
        }

        // End the game session if there are not enough active players left
        if (gameLogic.isGameRunning() && gameLogic.getActivePlayerCount() <= 1) {
            stopGame("NO_WINNER:Only one player left. Game ended.");
        }

        updateAllClients();
    }

    /**
     * Sends a message specifically to players currently in the active game/waiting room.
     * @param message The string message to broadcast.
     */
    private static void broadcastToGame(String message) {
        for (NewClient client : clients) {
            if (waitingRoom.contains(client.getPlayerName())) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Sends a message to every single connected client, regardless of their game state.
     * @param message The string message to broadcast.
     */
    private static void broadcast(String message) {
        for (NewClient client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * @return The current number of players in the waiting room.
     */
    public static synchronized int getWaitingCount() {
        return waitingRoom.size();
    }

    /**
     * Registers a player as 'Connected' in the Lobby list.
     * @param playerName The name of the player to register.
     */
    public static synchronized void AddConnectedPlayer(String playerName) {
        if (!connectedPlayers.contains(playerName)) {
            connectedPlayers.add(playerName);
            System.out.println("CONNECTED: " + playerName);
            updateAllClients();
        }
    }

    /**
     * Verifies if a username is already occupied.
     * @param playerName The name to check.
     * @return true if the name is taken, false otherwise.
     */
    public static synchronized boolean isNameTaken(String playerName) {
        return connectedPlayers.contains(playerName);
    }

    /**
     * Broadcasts the updated lists of connected players and waiting room status to everyone.
     * Also updates the play button status (enabled/disabled) for all clients.
     */
    public static synchronized void updateAllClients() {
        String players = "WAITING:" + String.join(",", waitingRoom);
        String connected = "PLAYERS:" + String.join(",", connectedPlayers);
        
        // Button should be disabled if game is running or room is at max capacity
        boolean shouldDisable = gameLogic.isGameRunning() || waitingRoom.size() >= 4;
        String playStatus = shouldDisable ? "DISABLE_PLAY" : "ENABLE_PLAY";

        for (NewClient client : clients) {
            client.sendMessage(connected);
            client.sendMessage(players);
            client.sendMessage(playStatus); 
        }
    }

    /**
     * Terminates the game session, clears participant lists, and notifies all connected clients.
     * @param message The termination message (e.g., winner announcement or timeout).
     */
    public static synchronized void stopGame(String message) {
        // 1. Terminate game logic
        gameLogic.endGame(); 
        
        // 2. Clear the waiting list completely
        waitingRoom.clear(); 
        
        // 3. Send the end message and final leaderboard to all clients (including lobby)
        broadcast(message); 
        broadcast(gameLogic.getWinnerListMessage());
        
        // 4. Update all client UIs to reflect the cleared room and lobby status
        updateAllClients();
        
        System.out.println("Game stopped and Waiting Room cleared.");
    }

    /**
     * Manually removes a player from the waiting room list.
     * @param playerName The name of the player leaving.
     */
    public static synchronized void removeFromWaitingRoom(String playerName) {
        waitingRoom.remove(playerName);
        System.out.println(playerName + " left the waiting room.");
    }
    
} // class end
