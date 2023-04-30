package server.sessionHandling;

import server.examsHelper.ExamsHelper;

import java.util.*;

public final class AdminSession extends AbstractSession {
    @Override
    public void update() {
        //ma il comportamento può essere anche diverso (es. può richiedere
        availableExams = examsHelper.retrieveAllExams();
    }

    /**
     * Comandi disponibili per un admin.
     */
    private enum AdminCommand  {
        LIST_AVAILABLE_EXAMS {
            @Override
            public String toString() {
                return ordinal() + ") Elencare appelli disponibili";
            }
        },
        INSERT_NEW_EXAM {
            public String toString() {
                return ordinal() + ") Inserire nuovo appello d'esame";
            }
        },
        QUIT {
            @Override
            public String toString() {
                return ordinal() + ") Logout";
            }
        },
        GO_BACK {
            @Override
            public String toString() {
                return ordinal() + ") Indietro";
            }
        },
        INSERT_NEW_STUDENT {
            @Override
            public String toString() {
                return ordinal() + ") Inserisci un nuovo studente nel sistema";
            }
        }
    }

    public AdminSession(ExamsHelper helper, int id) {
        super(helper, id);
        state = AdminSessionState.INITIAL_STATE;
    }
    private static final Map<Integer, AdminCommand> cmdCode;//mappa <idComando, comando>

    static {
        cmdCode = new HashMap<>();
        for (AdminCommand a : AdminCommand.values()) cmdCode.put(a.ordinal(), a);
    }

    private enum AdminSessionState implements SessionState<AdminSession> {
        INITIAL_STATE {
            @Override
            public Iterable<String> entryAction(AdminSession session) {
                session.stateChanged = false;
                CommandBuilder cb = new CommandBuilder("Benvenuto nel sistema laboratorio");
                cb.append(AdminCommand.QUIT).
                        append(AdminCommand.LIST_AVAILABLE_EXAMS).
                        append(AdminCommand.INSERT_NEW_STUDENT);
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, AdminSession session) {
                int cmd = validateNumericInput(cmdCode);
                if (cmd != -1) {
                    switch (AdminSession.cmdCode.get(cmd)) {
                        case LIST_AVAILABLE_EXAMS:
                            session.transition(LIST_EXAMS);
                            break;
                        case INSERT_NEW_STUDENT:
                            session.transition(NEW_USER);
                            break;
                        case QUIT:
                            session.logout();
                            break;
                        default:
                            session.transition(this);
                    }
                } else session.transition(this);
            }
        },
        NEW_EXAM {
            @Override
            public Iterable<String> entryAction(AdminSession session) {
                session.stateChanged = false;
                CommandBuilder cb = new CommandBuilder(AdminCommand.INSERT_NEW_EXAM.toString());
                cb.append(AdminCommand.GO_BACK); //una possibile estensione è l'introduzione di un examBuilder,
                // oggetto basato su logica a stati per "costruire" gli esami (simile concettualmente a ExamHandler)
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, AdminSession session) {
                //TEST
                //session.examsHelper.addExam(Test.testExamsGenerator(new Random().nextInt()));
                //TEST
                session.transition(LIST_EXAMS);
            }
        },
        NEW_USER {
            @Override
            public Iterable<String> entryAction(AdminSession session) {
                session.stateChanged = false;
                CommandBuilder cb = new CommandBuilder("Inserimento di un nuovo utente nel sistema");
                //TEST
                //TEST
                cb.append(AdminCommand.GO_BACK);//è possibile introdurre un componente userBuilder
                //che gestisca la "costruzione" e l'inserimento di un utente
                return cb.build();
            }

            @Override
            public void callback(String cmdCode, AdminSession session) {
                session.transition(INITIAL_STATE); //comportamento naive
            }
        },
        LIST_EXAMS {
            @Override
            public Iterable<String> entryAction(AdminSession s) {
                s.stateChanged = false;
                s.retrieveExams();
                return new CommandBuilder("Lista di tutti gli esami disponibili")
                        .append(s.availableExams).
                                append(AdminCommand.INSERT_NEW_EXAM)
                        .append(AdminCommand.GO_BACK).build();
            }

            @Override
            public void callback(String cmdCode, AdminSession session) {
                int cmd = validateNumericInput(cmdCode);
                if(cmd!=-1) {
                    switch (AdminSession.cmdCode.get(cmd)) {
                        case INSERT_NEW_EXAM:
                            session.transition(NEW_EXAM);
                            break;
                        case GO_BACK:
                            session.transition(INITIAL_STATE);
                            break;
                        default:
                            session.transition(this);
                    }
                }else session.transition(this);
            }
        };
    }//AdminSessionState

}

