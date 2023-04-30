package server.testGeneration;

import server.examHandler.exam.Answer;
import server.examHandler.exam.ConcreteQuestion;
import server.examHandler.exam.Exam;
import server.examHandler.exam.Question;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

public class TestExamGenerator {
    private TestExamGenerator(){}
    public static Exam testExamsGenerator(int EXAMID) {
        Answer a = new Answer("Risposta", 'a');
        Answer b = new Answer("Risposta", 'b');
        Answer c = new Answer("Risposta", 'c');
        Answer d = new Answer("Risposta", 'd');
        List<Answer> question = new LinkedList<>();
        question.add(a);
        question.add(b);
        question.add(c);
        question.add(d);
        //question ok
        List<Question> lq = new LinkedList<>();
        for(int i=0; i<10; i++) lq.add(new ConcreteQuestion("Domanda",i,question,a,5000));
        //questions ok
        return new Exam(lq, LocalDate.now(), LocalTime.now().plusSeconds(25),0,50000,EXAMID,1,"Software Engineering");
    }
}
