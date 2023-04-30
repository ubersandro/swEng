package server.examHandler.exam;

import java.util.Iterator;
import java.util.List;

/**
 * Concretizza l'interfaccia Multiple Choice Timed Question.
 */
public final class ConcreteQuestion implements Question {
    private final String question;
    private final int id;
    private final List<Answer> possibleAnswers;
    private final Answer correctAnswer;
    private final long questionTime;

    public ConcreteQuestion(String question, int id, List<Answer> possibleAnswers, Answer correctAnswer, long questionTime) {
        this.question = question;
        this.id = id;
        this.possibleAnswers = possibleAnswers;
        this.correctAnswer = correctAnswer;
        this.questionTime = questionTime;
    }

    @Override
    public char getAnswer() {
        return correctAnswer.getAnswerNumber();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public long getTime() {
        return questionTime;
    }

    @Override
    public String getQuestion() {
        return question;
    }

    @Override
    public Iterator<String> iterator() {
        return new AnswersIterator();
    }

    /**
     * Si avvale del design pattern iterator per regolare l'accesso al testo della domanda e alle sue risposte. Le stringhe sono gestite
     * secondo questa logica perchè in tal modo le consolle client (siano essere a riga di comando o con GUI) possono avvalersi
     * di un metodo di rappresentazione dei dati in arrivo dalla sessione generale (si passa per oggetti Iterable<String>) e quindi del tutto staccato
     * dalle classi concrete Question e Answer. E' però vero che l'agente consolle, non conoscendo le classi concrete, deve essere in grado di processare
     * a dovere le stringhe per realizzare un'interazione con gli oggetti Session (ad es. deve poter estrarre i caratteri delle risposte).
     */

    private class AnswersIterator implements  Iterator<String>{
        private Iterator<Answer> it = null;

        /**
         * Alla prima iterazione la variabile it è null perchè la prima stringa ad essere restituita è proprio il testo della domanda.
         * In seguito, viene inizializzato all'iteratore dell'oggetto linkedlist che raccoglie le possibili risposte.
         * @return
         */
        @Override
        public boolean hasNext() {
            return it == null || it.hasNext();
        }

        @Override
        public String next() {
            if(it == null){
                it = possibleAnswers.iterator();
                return id+") "+ ConcreteQuestion.this.question; //restituisce la domanda prima di tutto
            }
            return it.next().toString();
        }
    }
}

