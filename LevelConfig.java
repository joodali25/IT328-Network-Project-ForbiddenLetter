public class LevelConfig {
    private int levelNumber; // nummber of levels 1 2 3 4 5
    private String topic; // eexamplex fruits 
    private char[] forbiddenLetters; // example A so client cant write A
    private int minLength; // if its 5 -> player can write 5 or less
    private int levelTimeSeconds; // sec for this level ( 2 & 5 )
    private String[] acceptedWords; 

    // constructor
    public LevelConfig(int levelNumber, String topic, char[] forbiddenLetters,
                       int minLength, int levelTimeSeconds, String[] acceptedWords) {
        this.levelNumber = levelNumber;
        this.topic = topic;
        this.forbiddenLetters = forbiddenLetters;
        this.minLength = minLength;
        this.levelTimeSeconds = levelTimeSeconds;
        this.acceptedWords = acceptedWords;
    }
// getters so we can read level info outside the class
    public int getLevelNumber() { return levelNumber; }
    public String getTopic() { return topic; }
    public char[] getForbiddenLetters() { return forbiddenLetters; }
    public int getMinLength() { return minLength; }
    public int getLevelTimeSeconds() { return levelTimeSeconds; }
    public String[] getAcceptedWords() { return acceptedWords; }
// collect all details of level in one string 
    public String toProtocolMessage() {
        return "LEVEL_RULES:" + levelNumber + ":" + topic + ":" +
                new String(forbiddenLetters) + ":" + minLength + ":" + levelTimeSeconds;
    }
}
