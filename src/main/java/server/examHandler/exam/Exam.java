package server.examHandler.exam;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Astrae il concetto di esame.
 *  Design patterns used:
 * 1) Iterator
 */
@SuppressWarnings("unused")
public final class Exam implements Iterable<Question>, Serializable {//metodo per calcolare punteggio!
    private final List<Question> questions;
    private final LocalDate dueDate;
    private final LocalTime dueTime;
    private final int MINIMUM_SCORE;
    private final int EXAM_CODE;
    private final int subjectID;
    private final String subject;
    private final long EXAM_TIME;

    public int getSubjectID() {
        return subjectID;
    }

    public String getSubject() {
        return subject;
    }

    public int getID() {
        return EXAM_CODE;
    }

    public Exam(List<Question> q, LocalDate date, LocalTime dueTime, int minimumScore, long examTime, int code, int subID, String sub){
        questions = new LinkedList<>(q); //copia delle domande
        //eventuali check sulle domande
        dueDate = date;
        this.dueTime = dueTime;
        MINIMUM_SCORE = minimumScore;
        EXAM_TIME = examTime;
        EXAM_CODE = code;
        subject = sub;
        subjectID = subID;
    }

    public long getExamTime() {
        return EXAM_TIME;
    }

    public List<Question> getQuestions() {
        return questions;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public LocalTime getDueTime(){return dueTime;}

    public int getMinimumScore() {
        return MINIMUM_SCORE;
    }

    public int getQuestionsNumber(){
        return questions.size();
    }

    @Override
    public Iterator<Question> iterator() {
        return questions.iterator();
    }
    @Override
    public String toString(){
        return "APPELLO CODICE : "+EXAM_CODE+" DATA : "+dueDate+" ORA : "+dueTime +" MATERIA :"+subject +" CODICE MATERIA : "+subjectID +" DURATA:"+EXAM_TIME/1000;
    }
}
