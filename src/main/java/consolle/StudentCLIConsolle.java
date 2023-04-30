package consolle;


import server.laboratory.Endpoint;
import server.laboratory.StudentEndpoint;
import server.laboratory.credentialsHelper.InvalidIdentifierException;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

/**
 * It models a CLIConsolle, which is the same for both admin and students. It is a Remote object but it doesn't need to be
 * published to the RMIRegistry in use because a pointer to this object is passed by the Laboratory to the Session.
 */
public final class StudentCLIConsolle extends AbstractCLIConsolle {

    public StudentCLIConsolle(Endpoint e) {
        super(e);
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, InterruptedException {
        Registry registry = LocateRegistry.getRegistry(); //solo un test
        System.out.println(registry != null ? "Registry ok..." : "FAIL");
        if (registry != null) {
            StudentEndpoint uep = (StudentEndpoint) registry.lookup("LabUserEndPoint");
            System.out.println("Lookup ok...");
            Thread console = new StudentCLIConsolle(uep);
            console.start();
            System.out.println("Consolle started...");
            console.join();
        }
    }

    @Override
    public void login() throws InvalidIdentifierException {
        try {
            StudentEndpoint uep = (StudentEndpoint) laboratory;
            //parsing delle credenziali dell'utente id, scadenza, matricola
            String in = "";
            int mat = -1;
            int id = -1;
            LocalDate date = null;
            System.out.println("Inserire numero di matricola");
            mat = parseNumericValue();
            System.out.println("Inserire ID di un documento di identità valido");
            id = parseNumericValue();
            date = parseDate();
            System.out.println("Depositare tutti i dispositivi elettronici in proprio possesso e confermare l'avvenuto deposito premendo INVIO");
            sc.readLine();
            session = uep.studentLogin(id, date, mat);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
