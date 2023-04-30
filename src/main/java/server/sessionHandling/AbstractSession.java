package server.sessionHandling;

import utils.DPobserver.Observer;
import server.examsHelper.ExamsHelper;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractSession implements Session, Observer, Remote, Serializable {
    protected final int userID;
    protected final ExamsHelper examsHelper;
    protected @SuppressWarnings("rawtypes") SessionState state;
    protected boolean sessionIsActive;
    protected boolean stateChanged;
    protected List<String> availableExams;

    protected AbstractSession(ExamsHelper helper, int ID){
        examsHelper = helper;
        this.userID = ID;
        sessionIsActive = true;
        examsHelper.attach(this);
    }

    protected void transition(@SuppressWarnings("rawtypes") SessionState x) {
        synchronized (this) {
            state = x;
            stateChanged = true;
        }
    }


    protected void logout() {
        examsHelper.detach(this);
        synchronized (this) {
            stateChanged = true;
            sessionIsActive = false;
        }
    }
    @Override
    public boolean isOver() {
        synchronized (this) {
            return !sessionIsActive;
        }
    }

    @Override @SuppressWarnings("unchecked")
    public Iterable<String> getMessage() {
        return state.entryAction(this);
    }

    @Override @SuppressWarnings("unchecked")
    public void submitCommand(String cmd) {
        state.callback(cmd, this);
    }

    @Override
    public boolean stateChanged() throws RemoteException {
        synchronized (this){
            return stateChanged;
        }
    }

    @SuppressWarnings({"rawtypes", "unused"})
    protected SessionState getState(){
        synchronized (this) {
            return state;
        }
    }

    protected void retrieveExams() {
        if(availableExams==null) availableExams = examsHelper.retrieveAllExams();
    }

    //metodi e sottoclassi di utilità
    protected static class CommandBuilder {
        private final List<String> l;

        public CommandBuilder(String title) {
            l = new LinkedList<>();
            l.add(title);
        }

        public CommandBuilder(Iterable<String> it) {
            l = new LinkedList<>();
            for (String s : it) l.add(s);
        }

        public CommandBuilder append(Object c) {
            l.add(c.toString());
            return this;
        }

        public Iterable<String> build() {
            return l;
        }
    }

    /**
     * @param cmdCode stringa che rappresenta il comando sottomesso dall'utente.
     * @return Un intero diverso da -1 corrispondente ad un comando solo se il valore cmdCode è un valore numerico
     * pari al codice di uno dei comandi disponibili, -1 altrimenti.
     */
    protected static int validateNumericInput(String cmdCode) {
        if (cmdCode.matches("[0-9]+") && StudentSession.cmdCode.containsKey(Integer.parseInt(cmdCode)))
            return Integer.parseInt(cmdCode);
        return -1;
    }

}