package server.examHandler.exam;

import java.io.Serializable;

/**
 * Oggetto iterabile, con tipo generico attualizzato in String. Permette di iterare sulle possibili risposte della domanda e sul testo del quesito stesso.
 * Ogni domanda è,infatti, a risposta multipla ed è caratterizzata da un tempo massimo per rispondere.
 */
public interface Question extends Iterable<String>, Serializable {
    long getTime();

    /**
     *
     * @return restituisce il testo del quesito.
     */
    String getQuestion();

    /**
     *
     * @return Restituisce la risposta, una lettera.
     */
    char getAnswer();

    /**
     *
     * @return Restituisce il numero della domanda.
     */
    int getID();
}
