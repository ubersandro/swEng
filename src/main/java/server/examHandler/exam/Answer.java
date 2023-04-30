package server.examHandler.exam;

import java.io.Serializable;

/**
 * Questa astrazione modella il concetto di risposta ad un quesito.
 */
public class Answer implements Serializable {
    private char answerNumber;
    private String answerText;

    public Answer(String s,char x){
        answerNumber = x;
        answerText = s;
    }

    public String getAnswerText() {
        return answerText;
    }
    public char getAnswerNumber() {
        return answerNumber;
    }
    @Override public String toString(){
        return answerNumber+") "+ answerText;
    }
}

