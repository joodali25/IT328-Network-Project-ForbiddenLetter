public class GameRules {

    public static boolean containsForbiddenLetter(String word, char[] forbiddenLetters) {
        word = word.toLowerCase();

        for (char letter : forbiddenLetters) {
            if (word.indexOf(Character.toLowerCase(letter)) != -1) {
                return true;
            }
        }
        return false;
    }

    public static boolean meetsMinLength(String word, int minLength) {
        return word.length() >= minLength;
    }

    public static boolean isTopicRelated(String word, String[] acceptedWords) {
        word = word.toLowerCase();

        for (String accepted : acceptedWords) {
            if (accepted.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }
}
