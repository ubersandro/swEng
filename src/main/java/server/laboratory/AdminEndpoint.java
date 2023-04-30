package server.laboratory;

import server.sessionHandling.Session;
import server.laboratory.credentialsHelper.InvalidIdentifierException;


import java.rmi.RemoteException;
import java.time.LocalDate;

public interface AdminEndpoint extends Endpoint {
    Session adminLogin(int ID, LocalDate d) throws RemoteException, InvalidIdentifierException;
}
