package utils.dbManagement;

import server.examHandler.exam.Exam;

import java.util.List;

/**
 * Interfaccia del componente preposto a gestire la persistenza nel sistema laboratorio.
 * Assunzioni di base :
 * 1) Possono esistere diversi appelli di una stessa materia in quanto ogni appello è identificato da un ID di appello e non dal ID della materia.
 */
public interface DBManager{
    /**
     * Verifica l'esistenza di una prenotazione di uno studente con matricola mat all'esame con id examID.
     * @param mat numero di matricola dell'utente.
     * @param examID codice identificativo dell'esame.
     * @return true se la prenotazione esiste, false altrimenti.
     */
    boolean enrollmentLookup(int mat, int examID);

    /**
     * Ricerca gli utenti per matricola (una matricola identifica univocamente l'utente nel sistema) e confronta il CF dell'utente
     * con matricola mat con il CF fornito come parametro al metodo.
     * @param mat
     * @param CF
     * @return Restituisce true se i dati sono coerenti.
     */

    boolean studentLookup(int mat, String CF);

    /**
     * Il metodo verifica se esiste un admin con questo id.
     * @param documentID id del documento.
     * @return true se esiste
     */
    boolean adminLookup(int documentID);

    /**
     * Ricerca gli utenti per matricola (una matricola identifica univocamente l'utente nel sistema)
     * @param mat
     * @return Restituisce un oggetto studente, ammesso che esista uno studente con matricola mat, null altrimenti.
     */

    Student studentLookup(int mat);
    /**
     * Permette di recuperare un esame.
     * @param examID
     * @return null se non esiste, esame se esiste.
     */
    Exam examLookup(int examID);

    /**
     * Aggiunge la prenotazione dell'utente con matricola mat per l'esame examID, a patto che non esista già
     * (una prenotazione è univocamente identificata dalla coppia matricola-examID).
     * @param mat
     * @param examID
     * @return true se la prenotazione è stata aggiunta, false altrimenti.
     */
    boolean addEnrollment(int mat, int examID);

    /**
     *
     * @param mat
     * @return Prenotazioni dell'utente con matricola mat.
     */
    List<Exam> getUserEnrollments(int mat);

    /**
     * Restituisce tutti gli esami inseriti nel sistema .
     * @return
     */
    List<Exam> getAllExams();

    /**
     * Cerca tra tutti gli esami superati dall'utente con matricola mat per verificare
     * se ha superato l'esame della materia identificata dal ID subjectID.
     * @param subjectID
     * @param mat
     * @return -1 se l'esame non è stato superato, il voto se è stato superato.
     */
    int resultLookup(int subjectID, int mat);
    boolean addResult(int mat, int examID, int score);
    //estensioni future

    /**
     * Aggiunge un appello di esame (a patto che non ne esista uno con lo stesso ID).
     * @param e
     * @return true se l'esame è stato aggiunto .
     */
    boolean addExam(Exam e);
    boolean addStudent(Student s);
    boolean removeEnrollment(int mat, int examID);
    boolean removeExam(int examID);
    boolean removeStudent(Student s);
}
