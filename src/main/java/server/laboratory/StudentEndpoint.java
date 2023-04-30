package server.laboratory;

import server.sessionHandling.Session;
import server.laboratory.credentialsHelper.InvalidIdentifierException;

import java.time.LocalDate;
import java.rmi.RemoteException;

public interface StudentEndpoint extends Endpoint {
    Session studentLogin(int ID, LocalDate d, int mat) throws RemoteException, InvalidIdentifierException;
}
