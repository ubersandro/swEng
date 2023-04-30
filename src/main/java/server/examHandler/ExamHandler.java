package server.examHandler;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Future;


public interface ExamHandler extends Serializable {
    Future<Boolean> startExam();
    boolean examIsOver();
    String getRightAnswersAndScore();
    Future<Boolean> getCurrentQuestionTimer();
    Iterable<String> getNextQuestion();
    void answerCurrentQuestion(String ans);
    void submitExam();
    void abandonExam();
    boolean examPassed();
    int getScore();
    int getExamID();
    LocalDate getDueDate();
    LocalTime getDueTime();
    long getCurrentQuestionTime();
}
