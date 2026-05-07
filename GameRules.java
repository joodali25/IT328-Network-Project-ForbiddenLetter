public class GameRules {
// check if there is a forbidden letter
    public static boolean containsForbiddenLetter(String word, char[] forbiddenLetters) {
        word = word.toLowerCase();
// walk on array forbidden letter and check if word match
        for (char letter : forbiddenLetters) {
            if (word.indexOf(Character.toLowerCase(letter)) != -1) {
                return true;
            }
        }
        return false;
    }
// compare len of word with minlength
    public static boolean meetsMinLength(String word, int minLength) {
        return word.length() >= minLength;
    }
// check if word is realated to topic
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
