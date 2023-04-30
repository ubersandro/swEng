package server.sessionHandling;

import server.examHandler.ExamHandler;
import server.examsHelper.ExamsHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * La classe generalizza AbstractSession al fine di catturare le esigenze di uno studente.
 */
public final class StudentSession extends AbstractSession {
    public final static Map<Integer, StudentCommand> cmdCode;
    private static final int TOLERANCE = 1;
    static {
        cmdCode = new HashMap<>();
        for (StudentCommand cmd : StudentCommand.values())
            cmdCode.put(cmd.ordinal(), cmd);
    }

    //studentOnly
    private final int mat;
    private ExamHandler examHandler;
    private List<String> userEnrollments;
    //studentOnly

    public StudentSession(ExamsHelper helper/*puntatore all'unica istanza esistente*/, int ID, int mat) {
        super(helper, ID);
        //userOnly
        this.mat = mat;
        state = StudentSessionState.INITIAL_STATE;
    }

    private void retrieveEnrollments() {
        userEnrollments = examsHelper.retrieveEnrollments(mat);
    } //studentOnly

    private void prepareExam() {
        Future<Boolean> timer = examHandler.startExam();
        Thread y = new Thread(() -> {
            try {
                timer.get(); //in un'estensione futura potrebbe anche essere restituito alla consolle utente
                System.err.println("timer >>>>> TEMPO SCADUTO PER L'ESAME");
                StudentSession.this.endExam();
            } catch (InterruptedException | CancellationException iex) {
                //esame terminato per consegna -> va da solo in exam view
            } catch (ExecutionException exex) {
                exex.printStackTrace();
            }
        }); //examEnd notifier
        y.start(); //l'esame comincia qua
    }

    private void endExam() {
        synchronized (this) {
            if (state != StudentSessionState.EXAM_RESULT_VIEW && state!=StudentSessionState.POST_EXAM_VIEW ) {
                state = StudentSessionState.EXAM_RESULT_VIEW;
                stateChanged = true;

            }//if
        }//synchronized
    }

    private void resetState(){
        synchronized (this){
            stateChanged = false;
        }
    }

    @Override
    public void update() {
        availableExams = examsHelper.retrieveAllExams();
    }

    /**
     * Design pattern state. Gli oggetti stato (istanze del tipo enumerato) sono condivisi (statici) tra tutti gli oggetti
     * sessione, quindi non occorre ricorrere ad altri pattern per ottimizzare lo sfruttamento della memoria (es. FlyWeight).
     */
    private enum StudentSessionState implements SessionState<StudentSession> {
        INITIAL_STATE {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Benvenuti nel sistema laboratorio utente: " + s.userID +
                        ", matricola :" + s.mat + ". Inserire comando:");
                cb.append(StudentCommand.LIST_ENROLLMENTS).
                        append(StudentCommand.LIST_EXAMS).
                        append(StudentCommand.QUIT);
                return cb.build();
            }//entryAction

            @Override
            public void callback(String cmdCode, StudentSession session) {
                int cmd = validateNumericInput(cmdCode);
                if (cmd != -1)
                    switch (StudentSession.cmdCode.get(cmd)) {
                        case LIST_ENROLLMENTS:
                            session.transition(ENROLLMENTS_VIEW);
                            break;
                        case LIST_EXAMS:
                            session.transition(AVAILABLE_EXAMS_LIST);
                            break;
                        case QUIT:
                            session.logout();
                            break;
                    }//switch
                else session.transition(this);
            }//callback
        },
        AVAILABLE_EXAMS_LIST {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Lista degli esami disponibili (tutte le prenotazioni scadono DUE giorni prima della prova)\n");
                s.retrieveExams();
                for (String exam : s.availableExams) {
                    cb.append(exam);
                }
                cb.append(StudentCommand.ENROLL).
                        append(StudentCommand.GO_BACK);
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                int cmd = validateNumericInput(cmdCode);
                if (cmd != -1)
                    switch (StudentSession.cmdCode.get(cmd)) {
                        case ENROLL:
                            session.transition(ENROLLMENT);
                            break;
                        case GO_BACK:
                            session.transition(INITIAL_STATE);
                            break;
                    }//switch
                else session.transition(this);
            }
        },
        ENROLLMENTS_VIEW {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                s.retrieveEnrollments(); //aggiornamento automatico (Observer), questo metodo inizializza la lista solo alla prima iterazione
                CommandBuilder cb = new CommandBuilder("Lista delle prenotazioni effettuate\n");
                for (String enrollment : s.userEnrollments) {
                    cb.append(enrollment);
                }
                cb.append(StudentCommand.TAKE_AN_EXAM).
                        append(StudentCommand.GO_BACK);
                return cb.build();
            }//entryAction

            @Override
            public void callback(String cmdCode, StudentSession session) {
                int cmd = validateNumericInput(cmdCode);
                if (cmd != -1)
                    switch (StudentSession.cmdCode.get(cmd)) {
                        case GO_BACK:
                            session.transition(INITIAL_STATE);
                            break;
                        case TAKE_AN_EXAM:
                            session.transition(EXAM_FETCHING);
                            break;
                    }//switch
                else session.transition(this);
            } //callback
        },
        ENROLLMENT {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder(StudentCommand.PARSE_ENROLLMENT_DATA.toString());//è impl dependent ma si può gestire (lato consolle)
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                //formato dei dati mat#CF#examID
                if (cmdCode.matches("[0-9]+#[a-zA-Z0-9]+#[0-9]+")) {
                    String[] strings = cmdCode.split("#");
                    final int mat = Integer.parseInt(strings[0]);
                    final String CF = strings[1];
                    final int examID = Integer.parseInt(strings[2]);
                    if (session.examsHelper.enroll(mat, CF, examID)) session.transition(ENROLLMENTS_VIEW);
                    else session.transition(ENROLLMENT_ERROR); //i dati forniti non sono validi per il sistema
                } else session.transition(ENROLLMENT_ERROR); //i dati forniti non rispettano il pattern imposto
            } //callback
        },
        ENROLLMENT_ERROR {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Errore nell'inserimento dei dati della prenotazione");
                cb.append(StudentCommand.ENROLL).append(StudentCommand.GO_BACK);
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                int cmd = validateNumericInput(cmdCode);
                if (cmd != -1 && StudentSession.cmdCode.get(cmd) == StudentCommand.GO_BACK)
                    session.transition(AVAILABLE_EXAMS_LIST);
                else session.transition(ENROLLMENT);
            }
        },
        FETCH_ERROR {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Errore nell'inserimento dei dati dell'esame");
                cb.append(StudentCommand.TAKE_AN_EXAM).append(StudentCommand.GO_BACK);
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                int cmd = validateNumericInput(cmdCode);
                if (cmd != -1)
                    switch (StudentSession.cmdCode.get(cmd)) {
                        case GO_BACK:
                            session.transition(AVAILABLE_EXAMS_LIST);
                            break;
                        case TAKE_AN_EXAM:
                            session.transition(EXAM_FETCHING);
                            break;
                    }
                else session.transition(this);
            }
        },
        EXAM_FETCHING {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Specificare il codice dell'esame che si vuole intraprendere ed il numero di matricola" +
                        "nel seguente formato 'examID#mat'");
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                if (cmdCode.matches("[0-9]+#[0-9]+")) {
                    String[] s = cmdCode.split("#");
                    int examID = Integer.parseInt(s[0]), mat = Integer.parseInt(s[1]);
                    session.examHandler = session.examsHelper.retrieveExam(examID, mat);
                    if (session.examHandler != null) {
                        if (LocalDate.now().equals(session.examHandler.getDueDate())) {
                            if (LocalTime.now().isAfter(session.examHandler.getDueTime()) &&
                                    LocalTime.now().isBefore(session.examHandler.getDueTime().plusMinutes(TOLERANCE))) {
                                //è il momento di fare l'esame
                                //predisposizione
                                session.prepareExam();
                                session.transition(EXAM_MODE); // exam time
                            } else if (LocalTime.now().isBefore(session.examHandler.getDueTime()))
                                session.transition(WAITING_ROOM);//not yet the time
                            else if (LocalTime.now().isAfter(session.examHandler.getDueTime().plusMinutes(TOLERANCE)))
                                session.transition(EXAM_ALREADY_STARTED);
                        }//if today is the day
                        else {
                            session.transition(ENROLLMENTS_VIEW);
                        } //today is not the day
                    } else //handler == null
                        session.transition(FETCH_ERROR);
                } else session.transition(ENROLLMENTS_VIEW);
            }
        },
        WAITING_ROOM {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("L'esame non è ancora iniziato.");
                cb.append("La prova inizierà alle ore " + s.examHandler.getDueTime() + "\n");
                cb.append("Orario attuale: " + LocalTime.now() + "\n");
                cb.append("> Premere invio per eseguire refresh\n");
                cb.append(StudentCommand.GO_BACK);
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                if (session.examHandler.getDueTime().isBefore(LocalTime.now())) {//è il momento
                    session.prepareExam();
                    session.transition(EXAM_MODE);
                } else if (cmdCode.matches("[0-9]+")
                        && StudentSession.cmdCode.containsKey(Integer.parseInt(cmdCode)) &&
                        StudentSession.cmdCode.get(Integer.parseInt(cmdCode)).
                                equals(StudentCommand.GO_BACK))
                    session.transition(ENROLLMENTS_VIEW);
                else session.transition(this);
            }
        },
        EXAM_ALREADY_STARTED {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Errore nella procedura di inizio dell'esame!");
                cb.append("Tempo per intraprendere la prova scaduto, appello non più disponibile!\n");
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                session.transition(ENROLLMENTS_VIEW);
            }
        },
        EXAM_MODE {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                if (!s.examHandler.examIsOver()) { //viene verificato che la domanda essenzialmente esista
                    Iterable<String> currentQuestion = s.examHandler.getNextQuestion(); //questo metodo avvia il timer della domanda
                    Future<Boolean> currentQuestionTimer = s.examHandler.getCurrentQuestionTimer(); //recupera il future del timer
                    Thread x = new Thread(() -> {
                        try {
                            currentQuestionTimer.get();
                            if(!s.examHandler.examIsOver()) s.transition(EXAM_MODE); // se il tempo termina si procede con l'esame andando alla domanda successiva, se c'è
                        } catch (InterruptedException | CancellationException/*RISPOSTA*/ ex) {
                            // in caso di risposta, alla successiva ci va solo, il timer viene interrotto e l'eccezione ignorata!
                        } catch (ExecutionException ignored) {
                        }
                    });
                    x.start();//gestisce il timer della domanda
                    CommandBuilder cb = new CommandBuilder(currentQuestion);
                    cb.append("Tempo per rispondere alla domanda : "+s.examHandler.getCurrentQuestionTime()/1000+" secondi");
                    cb.append("Rispondere oppure:");
                    return cb.append(StudentCommand.NEXT_QUESTION).
                            append(StudentCommand.ABANDON_EXAM).
                            append(StudentCommand.SUBMIT_EXAM).append("\n\n").build();
                } else {//esame finito
                    s.endExam();
                    return new CommandBuilder("Esame terminato").build();
                }
            }//entryAction

            @Override
            public void callback(String cmdCode, StudentSession session) {
                if (cmdCode.matches("[0-9]+")) {
                    if (StudentSession.cmdCode.containsKey(Integer.parseInt(cmdCode)))//ignora i valori non ammissibili
                        switch (StudentSession.cmdCode.get(Integer.parseInt(cmdCode))) {
                            case ABANDON_EXAM:
                                session.examHandler.abandonExam();
                                session.endExam();
                                break;
                            case SUBMIT_EXAM:
                                session.examHandler.submitExam();
                                session.endExam();
                                break;
                            default:
                                session.transition(this); //saltare domanda
                        }//switch
                } else if (cmdCode.matches("[a-zA-Z]")) {
                    session.examHandler.answerCurrentQuestion(cmdCode);//la transizione di stato è gestita dal thread notifier nella entry action
                    session.transition(this);
                }
            }
        },
        EXAM_RESULT_VIEW {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Esame terminato!\n");
                cb.append(s.examHandler.getRightAnswersAndScore());
                if (s.examHandler.examPassed()) cb.append(StudentCommand.AKS_FOR_CONFIRMATION);
                else cb.append("ENTER) Premere INVIO per concludere.");
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                if (cmdCode.matches("[0-9]") && StudentSession.cmdCode.get(Integer.parseInt(cmdCode)) == StudentCommand.AKS_FOR_CONFIRMATION) {
                    //procedura di accettazione voto
                    session.examsHelper.registerResult(session.examHandler.getScore(), session.mat, session.examHandler.getExamID());
                }
                session.transition(POST_EXAM_VIEW);
            }
        },
        POST_EXAM_VIEW {
            @Override
            public Iterable<String> entryAction(StudentSession s) {
                s.resetState();
                CommandBuilder cb = new CommandBuilder("Vista post esame");
                cb.append("Cosa si intende fare adesso?");
                cb.append(StudentCommand.TAKE_AN_EXAM).append(StudentCommand.QUIT);
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, StudentSession session) {
                switch (StudentSession.cmdCode.get(Integer.parseInt(cmdCode))) {
                    case TAKE_AN_EXAM:
                        session.transition(AVAILABLE_EXAMS_LIST);
                        break;
                    default:
                        session.logout();
                }
            }
        }
    }//State

    private enum StudentCommand {
        NEXT_QUESTION {
            @Override
            public String toString() {
                return ordinal() + ") Passare alla prossima domanda";
            }
        },
        ABANDON_EXAM {
            @Override
            public String toString() {
                return ordinal() + ") Abbandonare l'esame";
            }
        },
        SUBMIT_EXAM {
            @Override
            public String toString() {
                return ordinal() + ") Sottomettere esame";
            }
        },
        LIST_EXAMS {
            @Override
            public String toString() {
                return ordinal() + ") Elencare appelli disponibili";
            }
        },
        QUIT {
            @Override
            public String toString() {
                return ordinal() + ") Logout";
            }
        },
        ENROLL {
            @Override
            public String toString() {
                return ordinal() + ") Prenotarsi ad un appello";
            }
        },
        LIST_ENROLLMENTS {
            @Override
            public String toString() {
                return ordinal() + ") Visualizzare prenotazioni effettuate";
            }
        },
        TAKE_AN_EXAM {
            @Override
            public String toString() {
                return ordinal() + ") Intraprendere esame";
            }
        },
        PARSE_ENROLLMENT_DATA {
            @Override
            public String toString() {
                return "Inserire i dati per la prenotazione nel seguente formato 'mat#codiceFiscale#codiceEsame' ";
            }
        },
        AKS_FOR_CONFIRMATION {
            @Override
            public String toString() {
                return ordinal() + ") Accettare voto";
            }
        },
        GO_BACK {
            @Override
            public String toString() {
                return ordinal() + ") Indietro";
            }
        }

    }
}//UserSession











