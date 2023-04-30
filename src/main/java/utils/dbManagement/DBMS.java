package utils.dbManagement;

import server.examHandler.exam.Exam;
import server.testGeneration.TestExamGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Istanze singleton di DBMS utilizzabili sulla scorta del contratto-interfaccia DBManager. Questo tipo enumerato contiene diverse implementazioni
 * della suddetta interfaccia, intercambiabili e settabili a piacere nel contesto dell'oggetto Laboratory. E' un punto di estensione futura.
 * Ad esempio, una possibile istanza del tipo enumerato può incapsulare un oggetto che, in accordo con il framework JDBC,
 * gestisce la persistenza interfacciandosi con un DBMS; in particolare, tale istanza potrebbe costruire queries da sottoporre ad una base di dati collegata
 * previa specifica di un simile comportamento nei metodi di interfaccia concretizzati e/o ridefiniti in tale sede.
 * Naturalmente, in mancanza di API ad hoc, la persistenza non è garantita.
 */

public enum DBMS implements DBManager {
    /**
     * Implementazione di DBManager naive funzionale al testing della modalità di esame
     * e della corretta visualizzazione di appelli e prenotazioni.
     */
    TEST_DB {
        @Override
        public boolean enrollmentLookup(int mat, int examID) {
            return mat==1 && examID==1;
        }

        @Override
        public boolean studentLookup(int mat, String CF) {
            return mat==1;
        }

        @Override
        public boolean adminLookup(int documentID) {
            return documentID==0;
        }

        /**
         * RICERCA NAIVE TESTING -> se l'utente è 1 può loggarsi, altrimenti è respinto.
         * @param mat
         * @return
         */
        @Override
        public Student studentLookup(int mat) {
            if (mat == 1) return new Student("CF1", 1, 1);//test
            return null;
        }

        @Override
        public Exam examLookup(int examID) {
            return TestExamGenerator.testExamsGenerator(1);
        }

        @Override
        public boolean addEnrollment(int mat, int examID) {
            return false;
        }

        @Override
        public boolean addStudent(Student s) {
            return false;
        }

        @Override
        public boolean addExam(Exam e) {
            return false;
        }

        @Override
        public boolean removeEnrollment(int mat, int examID) {
            return false;
        }

        @Override
        public boolean removeExam(int examID) {
            return false;
        }

        @Override
        public boolean removeStudent(Student s) {
            return false;
        }

        @Override
        public List<Exam> getAllExams() {
            List<Exam> l = new LinkedList<>();
            l.add(TestExamGenerator.testExamsGenerator(1)); //test
            l.add(new Exam(new LinkedList<>(), LocalDate.now().plusDays(7), LocalTime.now(),
                    890, 890, 890, 890, "SW ENG"));
            return l;
        }

        @Override
        public boolean addResult(int mat, int examID, int score) {
            return false;
        }

        @Override
        public List<Exam> getUserEnrollments(int mat) {
            List<Exam> l = new LinkedList<>();
            l.add(TestExamGenerator.testExamsGenerator(1)); //test
            return l;
        }

        @Override
        public int resultLookup(int examID, int mat) {
            return -1;
        }
    }
}
