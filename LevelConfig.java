public class LevelConfig {
    private int levelNumber;
    private String topic;
    private char[] forbiddenLetters;
    private int minLength;
    private int levelTimeSeconds;
    private String[] acceptedWords;

    public LevelConfig(int levelNumber, String topic, char[] forbiddenLetters,
                       int minLength, int levelTimeSeconds, String[] acceptedWords) {
        this.levelNumber = levelNumber;
        this.topic = topic;
        this.forbiddenLetters = forbiddenLetters;
        this.minLength = minLength;
        this.levelTimeSeconds = levelTimeSeconds;
        this.acceptedWords = acceptedWords;
    }

    public int getLevelNumber() { return levelNumber; }
    public String getTopic() { return topic; }
    public char[] getForbiddenLetters() { return forbiddenLetters; }
    public int getMinLength() { return minLength; }
    public int getLevelTimeSeconds() { return levelTimeSeconds; }
    public String[] getAcceptedWords() { return acceptedWords; }

    public String toProtocolMessage() {
        return "LEVEL_RULES:" + levelNumber + ":" + topic + ":" +
                new String(forbiddenLetters) + ":" + minLength + ":" + levelTimeSeconds;
    }
}
