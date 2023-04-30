package consolle;

import server.laboratory.Endpoint;
import server.laboratory.credentialsHelper.InvalidIdentifierException;
import server.sessionHandling.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.time.LocalDate;

/**
 * Classe che modella l'astrazione Consolle a riga di comando. Questa pre-implementazione identifica la consolle come un thread e demanda
 * alle classi concrete la gestione del login (si hanno procedure diverse per utenze diverse) .
 */
public abstract class AbstractCLIConsolle extends Thread implements Consolle {
    protected final Endpoint laboratory;
    protected final BufferedReader sc;
    protected Session session;
    protected boolean quit;

    protected AbstractCLIConsolle(Endpoint e) {
        laboratory = e;
        sc = new BufferedReader(new InputStreamReader(System.in/*bis*/));
    }

    @Override
    public void run() {
        while (!quit) {
            welcome();
            String in;
            try {
                in = sc.readLine();
                if (in.matches("[0-9]")) {
                    switch (Integer.parseInt(in)) {
                        case 0:
                            try{
                                login();
                                try {
                                    System.out.println("Login eseguito");
                                    sessionHandling();
                                } catch (RemoteException e) {
                                    System.err.println("Errore di comunicazione con il laboratorio.");
                                    e.printStackTrace();
                                }
                                logout();
                            } catch (InvalidIdentifierException iex) {
                                System.out.println(iex.getMessage());
                            }
                            break;
                        case 1:
                            quit();
                            break;
                    }//switch
                }//if
            } catch (IOException e) {
                e.printStackTrace();
            }
        }//while
    }//run


    @Override
    public void welcome() {
        System.out.println("Benvenuto nel sistema laboratorio! Inserisci il comando\n\t0) login\n\t1) quit");
    }

    @Override
    public void quit() {
        System.out.println("Arrivederci");
        quit = true;
        session = null;
    }

    @Override
    public void logout() {
        System.out.println("Sessione terminata");
        session = null;
    }

    //caratteristici
    protected void sessionHandling() throws RemoteException {
        while (!session.isOver()) {
            displayContent(session.getMessage());
            try {
                while (!session.stateChanged()) {
                    String cmd ;
                    if (System.in.available() == 0) {
                        Thread.sleep(1500);
                    } else {
                        cmd = sc.readLine();
                        session.submitCommand(cmd);
                    }
                }
            } catch (InterruptedException | IOException ignored) {
            }
        }//while
    }//startSession
    protected void displayContent(Iterable<String> state) { //metodo astratto
        for (String s : state) System.out.println(s);
    }
    protected int parseNumericValue(){
        int n = -1;
        String in = null;
        do{

            try {
                in = sc.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(in.matches("[0-9]+"))
                n = Integer.parseInt(in);
        }while(n == -1);
        return n;
    }
    protected LocalDate parseDate(){
        String in = null;
        LocalDate date = null;
        do{
            System.out.println("Inserire data di scadenza documento nel formato aaaa-mm-aa");
            try {
                in = sc.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(in.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")){//formato standard
                date = LocalDate.parse(in);
            }
        }while(date==null);
        return date;
    }

}
