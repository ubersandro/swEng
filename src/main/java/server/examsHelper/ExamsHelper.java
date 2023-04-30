package server.examsHelper;

import utils.DPobserver.Subject;
import server.examHandler.exam.Exam;
import server.examHandler.ExamHandler;

import java.util.List;

/**
 * Il componente è preposto alla gestione delle informazioni e dei servizi connessi agli esami.
 *
 * Assunzioni di base :
 * 1) Le date dei diversi appelli di una stessa materia non sono soggette a vincoli.
 * 2) Possono esistere diversi appelli di una stessa materia (no vincoli di numero).
 *
 * Una possibile implementazione potrebbe prevedere che l'oggetto in questione si comporti da listener nei confronti della
 * base di dati del laboratorio al fine di notificare a sua volta ai propri listener ciò che avviene lato db e che non necessariamente
 * passa dal componente stesso (es. possono essere aggiunti appelli senza che si usi questa interfaccia, il db ha "vita propria").
 */
public interface ExamsHelper extends Subject{ //è sempre vero che è un Subject ? NO
    /**
     * Metodo per eseguire la fetch di un esame. E' necessario essere prenotati ad un esame prima di poterlo recuperare ed eventualmente affrontare.
     * @param examID
     * @param mat
     * @return ExamHandler per sostenere l'esame o null in caso di prenotazione non esistente o errori .
     */
    ExamHandler retrieveExam(int examID, int mat);

    /**
     * Permette di prenotarsi per sostenere un esame. Un utente che ha già superato un esame non può prenotarsi allo stesso!
     *
     * @param mat
     * @param CF
     * @param examID
     * @return true se la prenotazione è andata a buon fine.
     */
    boolean enroll(int mat, String CF, int examID);
    /**
     * Metodo che restituisce una vista di tutti gli appelli inseriti e disponibili per la prenotazione (ci si può prenotare ad un
     * appello fino a due giorni prima della data dello stesso).
     * @return Lista di rappresentazioni sotto forma di stringa degli esami disponibili. Tutti gli studenti vedono tutti gli appelli di tutti gli esami, anche
     * quelli già sostenuti e superati (si veda il metodo per la prenotazione).
     */
    List<String> retrieveAllExams();

    /**
     * Restituisce tutte e sole le prenotazioni di un utente tali che l'esame
     * a cui lo stesso è prenotato non è ancora stato sostenunto
     * o comunque non si è ancora svolto.
     * @param mat
     * @return lista delle prenotazioni dell'utente con matricola mat.
     */
    List<String> retrieveEnrollments(int mat);

    /**
     * Permette di aggiungere un nuovo esame, a patto che le date siano compatibili con i seguenti requisiti :
     * 1) prenotabilità : non devono essere già scaduti i termini di prenotazione
     * 2) unicità : non ci devono essere esami uguali con lo stesso ID.
     * @param e
     * @return true se l'esame può essere aggiunto
     */
    boolean addExam(Exam e);

    /**
     * Permette di registrare un voto (previa accettazione utente) relativo ad un esame superato.
     * @param score
     * @param mat
     * @param examID
     */
    void registerResult(int score, int mat, int examID);
}
