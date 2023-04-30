package server.examHandler;

import server.examHandler.exam.Exam;
import server.examHandler.exam.Question;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Modella la procedura di svolgimento della prova di esame e gestisce la valutazione.
 * L'unità di misura dei tempi è il millisecondi [ms]
 */
public final class ConcreteExamHandler implements ExamHandler {
    private static final int CORRECT = 3, NOT_GIVEN = -1, INCORRECT = 0;//valori per la correzione
    private final ExecutorService ex;//regola l'esecuzione dei thread timer
    private final Exam exam; //oggetto esame
    private final Iterator<? extends Question> questionsIt; //per iterare sulle domande
    private final Map<Integer, String> answers;  //mappa <idDomanda, rispostaUtente>
    private Future<Boolean> examTimer, currentQuestionTimer; //thread timer
    private Question currentQuestion; //domanda corrente

    private boolean userQuitted = false;
    private int finalScore;

    public ConcreteExamHandler(Exam exam) {
        this.exam = exam;
        questionsIt = exam.iterator();
        ex = Executors.newFixedThreadPool(2); //sono attivi max due thread per volta (due timer)
        answers = new HashMap<>();
        finalScore = -1;
    }

    public static void main(String[] args) {
        //test modalità di esame
        /*List<MCTQuestion> lq = new LinkedList<>();
        Answer a = new Answer("Risposta", 'a');
        Answer b = new Answer("Risposta", 'b');
        Answer c = new Answer("Risposta", 'c');
        Answer d = new Answer("Risposta", 'd');
        List<Answer> la = new LinkedList<>();
        la.add(a); la.add(b); la.add(c); la.add(d);
        for(int i=0; i<10; i++) lq.add(new ConcreteMCTQuestion("Domanda",i,la,a,2000));
        Exam<MCTQuestion> e1 = new Exam<>(lq, LocalDate.now()
                .plusDays(3), LocalTime.now(),10,
                15000,1);

        ConcreteExamHandler handler = new ConcreteExamHandler(e1);
        try {
            handler.startExam();
            Thread x = new Thread(()->{
                try{
                    handler.examTimer.get();
                    System.out.println("Tempo esame esaurito");
                } catch (CancellationException e) {
                    System.out.println("TIMER ESAME CANCELLATO");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } );
            x.start();
            while (!handler.examIsOver()) {
                System.out.println("PROSSIMA DOMANDA");
                handler.getNextQuestion();
                if (new Random().nextDouble() > 0.6) {
                    handler.answerCurrentQuestion("a");
                    System.out.println("ho risposto");
                }
                else System.out.println("non ho risposto, attendo");
                Future<Boolean> f = handler.getCurrentQuestionTimer();
                try {
                    f.get();
                }catch (CancellationException ces){
                    //System.out.println("Risp ricevuta");
                }
            }
            x.join();
            System.out.println("ESAME FINITO");
            System.out.println(handler.getRightAnswersAndScore());
        }catch (InterruptedException | ExecutionException e) {
            //e.printStackTrace();
        }*/
        //fine test
    }

    @Override
    public Future<Boolean> startExam() { //va fatto PRIMA di chiedere examIsOver
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(exam.getExamTime());
            } catch (InterruptedException iex) {
                System.out.println(" EXAM > timer interrotto");
                //esame sottomesso oppure l'utente si è ritirato.
            }
        });
        examTimer = ex.submit(t, true);
        return examTimer;
    }

    /**
     * L'esame finisce quando non ci sono più domande da proporre oppure quando il timer dell'esame si è esaurito o è
     * stato cancellato (ritiro o sottomissione dell'utente).
     *
     * @return
     */

    @Override
    public boolean examIsOver() {
        synchronized (this) {
            if (!questionsIt.hasNext()) examTimer.cancel(true);
            return !questionsIt.hasNext() || examTimer.isDone();
        }
    }

    /**
     * @return Restituisce un modulo con tutte le risposte corrette e il punteggio dell'utente.
     */
    @Override
    public String getRightAnswersAndScore() {
        ex.shutdownNow(); //a fine esame si può spegnere l'executor.
        StringBuilder sb = new StringBuilder(400);
        for (Question q : exam) {
            sb.append(q.getQuestion());
            sb.append(" RISPOSTA CORRETTA : ");
            sb.append(q.getAnswer());
            sb.append('\n');
        }
        if (!userQuitted) {
            int x = computerUserScore();
            sb.append("PUNTEGGIO : ");
            sb.append(x);
            sb.append('\n');
            sb.append(x >= exam.getMinimumScore() ? "ESAME SUPERATO!" : "ESAME FALLITO!");
        } else sb.append("Grazie per il tentativo.");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Se il punteggio finale non è ancora stato calcolato lo calcola.
     *
     * @return Punteggio totalizzato dall'utente.
     */
    private int computerUserScore() {
        if (finalScore == -1) {
            int userScore = 0;
            for (Question q : exam) {
                if (answers.containsKey(q.getID())) {
                    String correctAns = String.valueOf(q.getAnswer());
                    if (answers.get(q.getID()).equals(correctAns)) userScore += CORRECT;
                    else userScore += INCORRECT;
                } else userScore += NOT_GIVEN;
            }
            finalScore = userScore;
        }
        return finalScore;
    }

    @Override
    public Future<Boolean> getCurrentQuestionTimer() {
        return currentQuestionTimer;
    }

    /**
     * Posto che l'esame non deve essere terminato (o per lo meno ci deve essere ancora qualche domanda non proposta) questo
     * metodo restituisce la prossima domanda ed inizializza un timer associato ad essa.
     *
     * @return
     */
    @Override
    public Iterable<String> getNextQuestion() {
        //se l'utente salta la domanda devo cancellare il timer della precedente
        if (currentQuestionTimer != null && !currentQuestionTimer.isCancelled()/*DOMANDA SALTATA*/)
            currentQuestionTimer.cancel(true);
        currentQuestion = questionsIt.next();
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(currentQuestion.getTime());
            } catch (InterruptedException ignored) {
            }
        }); //thread timer domanda
        currentQuestionTimer = ex.submit(t, true); //monitora il thread timer domanda
        return currentQuestion;
    }

    /**
     * Cambia lo stato della domanda corrente fissando la risposta fornita dall'utente al valore ans.
     *
     * @param ans
     */
    @Override
    public void answerCurrentQuestion(String ans) {
        synchronized (this) {
            answers.put(currentQuestion.getID(), ans);
            currentQuestionTimer.cancel(true);
        }
    }

    /**
     * Sottomissione dell'esame : si cancellano i timer e l'esame termina.
     */
    @Override
    public void submitExam() {
        synchronized (this) {
            currentQuestionTimer.cancel(true);
            examTimer.cancel(true);
        }
    }

    /**
     * Si cancellano i timer e si marca l'esame terminato per abbandono.
     */
    @Override
    public void abandonExam() {
        synchronized (this) {
            examTimer.cancel(true);
            currentQuestionTimer.cancel(true);
            userQuitted = true;
        }
    }

    /**
     * @return true in caso di esame superato.
     */
    @Override
    public boolean examPassed() {
        return !userQuitted && finalScore >= exam.getMinimumScore();
    }

    @Override
    public int getScore() {
        return computerUserScore();
    }

    @Override
    public int getExamID() {
        return exam.getID();
    }

    @Override
    public LocalDate getDueDate() {
        return exam.getDueDate();
    }

    @Override
    public LocalTime getDueTime() {
        return exam.getDueTime();
    }

    @Override
    public long getCurrentQuestionTime() {
        return currentQuestion.getTime();
    }
}
