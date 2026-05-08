import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameLogic {

    private LevelConfig[] levels;
    private int currentLevelIndex;
    private HashMap<String, Integer> scores;
    private HashSet<String> usedWordsThisLevel;
    private WordValidator validator;

    private final int TARGET_SCORE = 5;
    private boolean gameRunning;

    public GameLogic() {
        levels = new LevelConfig[] {
                // Level 1: one forbidden letter + easy topic
                new LevelConfig(1, "Fruits", new char[]{'a'}, 0, 0,
                        new String[]{"kiwi", "melon", "fig", "lemon", "plum", "berry"}),

                // Level 2: one forbidden letter + 1 minute timer
                new LevelConfig(2, "Animals", new char[]{'e'}, 0, 60,
                        new String[]{"cat", "dog", "lion", "wolf", "goat", "duck"}),

                // Level 3: two forbidden letters
                new LevelConfig(3, "Countries", new char[]{'a', 'i'}, 0, 0,
                        new String[]{"peru", "egypt", "chile", "oman", "qtr"}),

                // Level 4: one forbidden letter + word length rule
                new LevelConfig(4, "School", new char[]{'o'}, 6, 0,
                        new String[]{"pencil", "teacher", "student", "marker", "ruler"}),

                // Level 5: two forbidden letters + word length rule + 1 minute timer
                new LevelConfig(5, "Sports", new char[]{'e', 'a'}, 6, 60,
                        new String[]{"boxing", "hiking", "rowing", "skiing", "surfing"})
        };

        scores = new HashMap<>();
        usedWordsThisLevel = new HashSet<>();
        validator = new WordValidator();
        currentLevelIndex = 0;
        gameRunning = false;
    }

    public void startGame(ArrayList<String> players) {
        scores.clear();

        for (String player : players) {
            scores.put(player, 0);
        }

        currentLevelIndex = 0;
        usedWordsThisLevel.clear();
        gameRunning = true;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public LevelConfig getCurrentLevel() {
        return levels[currentLevelIndex];
    }

    public String getCurrentLevelMessage() {
        return getCurrentLevel().toProtocolMessage();
    }

    public ValidationResult submitWord(String playerName, String word) {
        if (!gameRunning) {
            return new ValidationResult(false, "INVALID_WORD:Game is not running");
        }

        ValidationResult result = validator.validate(word, getCurrentLevel(), usedWordsThisLevel);

        if (!result.isValid()) {
            return result;
        }

        int newScore = scores.getOrDefault(playerName, 0) + 1;
        scores.put(playerName, newScore);

        return result;
    }

    public boolean moveToNextLevel() {
        if (currentLevelIndex < levels.length - 1) {
            currentLevelIndex++;
            usedWordsThisLevel.clear();
            return true;
        }

        return false;
    }

    public boolean hasWinner(String playerName) {
        return getScore(playerName) >= TARGET_SCORE;
    }

    public int getScore(String playerName) {
        return scores.getOrDefault(playerName, 0);
    }

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

    public void removePlayer(String playerName) {
        scores.remove(playerName);
    }

    public int getActivePlayerCount() {
        return scores.size();
    }

    public void endGame() {
        gameRunning = false;
    }
    
    public String getWinnerListMessage() {
    StringBuilder sb = new StringBuilder("WINNER_LIST:");

    scores.entrySet()
            .stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(entry -> {
                sb.append(entry.getKey())
                  .append("=")
                  .append(entry.getValue())
                  .append(",");
            });

    if (sb.charAt(sb.length() - 1) == ',') {
        sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
}
    
}//class end
