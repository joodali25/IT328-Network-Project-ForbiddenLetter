import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Handles the core game logic, including level management, scoring, 
 * and tracking game state.
 */
public class GameLogic {

    private LevelConfig[] levels;
    private int currentLevelIndex;
    private HashMap<String, Integer> scores;
    private HashSet<String> usedWordsThisLevel;
    private WordValidator validator;

    private final int TARGET_SCORE = 3;
    private boolean gameRunning;

    /**
     * Initializes the game logic with a predefined set of levels and game components.
     */
    public GameLogic() {
        levels = new LevelConfig[] {
                // Level 1: One forbidden letter + easy topic
                new LevelConfig(1, "Fruits", new char[]{'a'}, 0, 0,
                        new String[]{"kiwi", "melon", "fig", "lemon", "plum", "berry"}),

                // Level 2: One forbidden letter + 1 minute timer
                new LevelConfig(2, "Animals", new char[]{'e'}, 0, 60,
                        new String[]{"cat", "dog", "lion", "wolf", "goat", "duck"}),

                // Level 3: Two forbidden letters
              new LevelConfig(3, "Countries", new char[]{'a', 'i'}, 0, 0,
                        new String[]{"peru", "egypt", "Greece", "Morocco", "Yemen"}),


                // Level 4: One forbidden letter + word length rule
                new LevelConfig(4, "School", new char[]{'o'}, 6, 0,
                        new String[]{"pencil", "teacher", "student", "marker", "ruler"}),

                // Level 5: Two forbidden letters + word length rule + 1 minute timer
                new LevelConfig(5, "Sports", new char[]{'e', 'a'}, 6, 60,
                        new String[]{"boxing", "hiking", "rowing", "skiing", "surfing"})
        };

        scores = new HashMap<>();
        usedWordsThisLevel = new HashSet<>();
        validator = new WordValidator();
        currentLevelIndex = 0;
        gameRunning = false;
    }

    /**
     * Resets the game state and scores for a new session with the given players.
     * @param players List of player names participating in the game.
     */
    public void startGame(ArrayList<String> players) {
        scores.clear();

        for (String player : players) {
            scores.put(player, 0);
        }

        currentLevelIndex = 0;
        usedWordsThisLevel.clear();
        gameRunning = true;
    }

    /**
     * @return true if a game session is currently active.
     */
    public boolean isGameRunning() {
        return gameRunning;
    }

    /**
     * @return The configuration object for the current level.
     */
    public LevelConfig getCurrentLevel() {
        return levels[currentLevelIndex];
    }

    /**
     * Formats the current level info into a protocol-compliant string for the client.
     * @return A formatted string with level details.
     */
    public String getCurrentLevelMessage() {
        return getCurrentLevel().toProtocolMessage();
    }

    /**
     * Validates a submitted word and updates the player's score if successful.
     * @param playerName Name of the player who submitted the word.
     * @param word The submitted word string.
     * @return A ValidationResult object containing success status and feedback message.
     */
    public ValidationResult submitWord(String playerName, String word) {
        if (!gameRunning) {
            return new ValidationResult(false, "INVALID_WORD:Game is not running");
        }

        ValidationResult result = validator.validate(word, getCurrentLevel(), usedWordsThisLevel);

        if (!result.isValid()) {
            return result;
        }

        // Increment score if word is valid
        int newScore = scores.getOrDefault(playerName, 0) + 1;
        scores.put(playerName, newScore);

        return result;
    }

    /**
     * Increments the level index and clears the used words list for the new level.
     * @return true if there is a next level to move to, false if the game has ended.
     */
    public boolean moveToNextLevel() {
        if (currentLevelIndex < levels.length - 1) {
            currentLevelIndex++;
            usedWordsThisLevel.clear();
            return true;
        }

        return false;
    }

    /**
     * Checks if a player has reached the score required to win.
     * @param playerName The player to check.
     * @return true if the player reached or exceeded TARGET_SCORE.
     */
    public boolean hasWinner(String playerName) {
        return getScore(playerName) >= TARGET_SCORE;
    }

    /**
     * Retrieves the current score of a specific player.
     * @param playerName The player's name.
     * @return The score, or 0 if player not found.
     */
    public int getScore(String playerName) {
        return scores.getOrDefault(playerName, 0);
    }

    /**
     * Generates a protocol message string containing all players' current scores.
     * @return Formatted string "SCORES:Player1=Score,Player2=Score..."
     */
    public String getScoresMessage() {
        StringBuilder sb = new StringBuilder("SCORES:");

        int count = 0;
        for (String player : scores.keySet()) {
            sb.append(player).append("=").append(scores.get(player));
            count++;

            if (count < scores.size()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * Removes a player from the scoring map.
     * @param playerName The player to remove.
     */
    public void removePlayer(String playerName) {
        scores.remove(playerName);
    }

    /**
     * @return The number of players currently in the active game.
     */
    public int getActivePlayerCount() {
        return scores.size();
    }

    /**
     * Sets the game running state to false.
     */
    public void endGame() {
        gameRunning = false;
    }
    
    /**
     * Creates a ranked list of winners and their scores, sorted from highest to lowest.
     * @return Formatted string "WINNER_LIST:Player1=Score,Player2=Score..."
     */
    public String getWinnerListMessage() {
        StringBuilder sb = new StringBuilder("WINNER_LIST:");

        // Sort players by score descending and build the string
        scores.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    sb.append(entry.getKey())
                      .append("=")
                      .append(entry.getValue())
                      .append(",");
                });

        // Remove trailing comma if present
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
    
} // class end
