package server.examsHelper;

import server.examHandler.ConcreteExamHandler;
import server.examHandler.ExamHandler;
import server.examHandler.exam.Exam;
import utils.DPobserver.Observer;
import utils.DPobserver.Subject;
import utils.dbManagement.DBMS;
import utils.dbManagement.DBManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Questa implementazione aderisce al design pattern Observer. In particolare, gli oggetti Session osservano l'oggetto ExamsHelper
 * al fine di aggiornare la propria lista degli appelli quando ne vengono aggiunti di nuovi.
 */


public enum ConcreteExamsHelper implements ExamsHelper, Subject {
    SINGLETON {
        private List<Observer> listeners = new LinkedList<>(); //non bisogna violare l'incapsulamento!
        private final DBManager db = DBMS.TEST_DB;
        //ci si può prenotare entro due giorni dall'appello
        //ci sono massimo 1 minuti di ritardo di tolleranza all'appello
        private static final int ENROLLMENT_DEADLINE = 2,TOLERANCE = 1; //1 minuto di tolleranza


        @Override
        public void registerResult(int score, int mat, int examID) {
            if (db.addResult(mat, examID, score))
                System.out.println("EXAMS_HELPER >> VOTO REGISTRATO CORRETTAMENTE ");
            else System.out.println("EXAMS_HELPER >> VOTO NON REGISTRATO");
        }

        @Override
        public synchronized void attach(Observer o) {
            listeners.add(o);
        }

        @Override
        public synchronized void detach(Observer o) {
            listeners.remove(o);
        }

        @Override
        public void notifyObservers() {
            for (Observer o : listeners) o.update();
        }

        @Override
        public ExamHandler retrieveExam(int examID, int mat) {
            Exam e = db.examLookup(examID); //recupero esame
            if (e == null) return null;
            if (db.enrollmentLookup(mat, examID) &&
                    e.getDueDate().compareTo(LocalDate.now()) == 0 &&
                    e.getDueTime().plusMinutes(TOLERANCE).compareTo(LocalTime.now()) > 0)
                return new ConcreteExamHandler(e); //se esiste e si è in tempo si può sostenere
            return null; //recupero fallito
        }

        @Override
        public boolean addExam(Exam e) {
            //check opzionali sugli esami da aggiungere
            LocalDate finePrenotazioni = e.getDueDate().minusDays(ENROLLMENT_DEADLINE);
            if (finePrenotazioni.compareTo(LocalDate.now()) < 0) return false;
            if (db.examLookup(e.getID()) != null) return false;
            if (db.addExam(e)) {
                notifyObservers(); //aggiornamento Observers
                return true;
            } else return false;
        }

        @Override
        public List<String> retrieveAllExams() {
            List<String> l = new LinkedList<>();
            for (Exam e : db.getAllExams()) {
                LocalDate enrollmentDeadline = e.getDueDate().minusDays(ENROLLMENT_DEADLINE);
                if (enrollmentDeadline.compareTo(LocalDate.now()) >= 0)
                    l.add(e.toString()); //solo se non sono scadute le prenotazioni !!
            }
            return l;
        }

        @Override
        public List<String> retrieveEnrollments(int mat) {
            List<String> l = new LinkedList<>();
            List<Exam> enrollments = db.getUserEnrollments(mat);
            if (enrollments != null)//potrebbero non esistere prenotazioni (in tal caso restituisce null)
                for (Exam e : enrollments)
                    if (db.resultLookup(e.getSubjectID(), mat) == -1 //esame non sostenuto
                            && (e.getDueDate().compareTo(LocalDate.now()) >= 0 ||
                            (e.getDueDate().compareTo(LocalDate.now()) == 0 &&
                                    e.getDueTime().plusMinutes(TOLERANCE).compareTo(LocalTime.now()) > 0))) //non è il giorno dell'esame oppure non è ora
                        l.add(e.toString());
                    else db.removeEnrollment(mat, e.getID()); //se l'esame è già passato rimuovo la prenotazione
            return l;
        }

        /**
         *Metodo per prenotarsi ad un esame.
         * @param mat numero di matricola
         * @param CF Codice fiscale
         * @param examID id dell'esame
         * @return true se la prenotazione è andata a buon fine.
         */
        @Override
        public boolean enroll(int mat, String CF, int examID) {
            Exam e = db.examLookup(examID);
            if (e == null || //l'appello non esiste
                    db.resultLookup(e.getSubjectID(), mat) != -1 || //esiste ma l'esame è già stato superato
                    (!db.studentLookup(mat, CF)) || //lo studente non è valido
                    db.enrollmentLookup(mat, examID))  // lo studente è già prenotato
                return false;
            return db.addEnrollment(mat, examID);
        }

    }
}
