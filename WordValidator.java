import java.util.HashSet; // save words without repetion 

public class WordValidator {
// check the word if the ruls apply 
    public ValidationResult validate(String word, LevelConfig level, HashSet<String> usedWordsThisLevel) {
        if (word == null || word.trim().isEmpty()) { // if the word empty or its blanks
            return new ValidationResult(false, "INVALID_WORD:Word cannot be empty"); 
        }

        word = word.trim().toLowerCase(); //trim to remove spaces ,and make is lower case
// unrepeated word 
        if (usedWordsThisLevel.contains(word)) {
            return new ValidationResult(false, "INVALID_WORD:Word already used in this level");
        }

        if (!GameRules.isTopicRelated(word, level.getAcceptedWords())) {
            return new ValidationResult(false, "INVALID_WORD:Word is not related to the topic");
        }

        if (GameRules.containsForbiddenLetter(word, level.getForbiddenLetters())) {
            return new ValidationResult(false, "INVALID_WORD:Word contains forbidden letter");
        }

        if (!GameRules.meetsMinLength(word, level.getMinLength())) {
            return new ValidationResult(false, "INVALID_WORD:Word is too short");
        }

        usedWordsThisLevel.add(word);
        return new ValidationResult(true, "VALID_WORD:" + word);
    }
}
