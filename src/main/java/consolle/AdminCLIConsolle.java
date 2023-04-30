package consolle;


import server.laboratory.AdminEndpoint;
import server.laboratory.Endpoint;
import server.laboratory.credentialsHelper.InvalidIdentifierException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public final class AdminCLIConsolle extends AbstractCLIConsolle {

    protected AdminCLIConsolle(Endpoint e) {
        super(e);
    }

    @Override
    public void login() throws InvalidIdentifierException {
        AdminEndpoint aep = (AdminEndpoint) laboratory;
        System.out.println("Inserire ID di un documento di identità valido");
        int id = parseNumericValue();
        System.out.println("Inserire data di scadenza del documento");
        try {
            session = aep.adminLogin(id, LocalDate.now().plusDays(1)); //test 0 -> l'utente si logga direttamente, altri valori bisogna verificare la data
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, InterruptedException {
        Registry registry = LocateRegistry.getRegistry();
        AdminEndpoint uep = (AdminEndpoint) registry.lookup("LabAdminEndpoint");
        Thread console = new AdminCLIConsolle(uep);
        console.start();
        console.join();
    }
}
