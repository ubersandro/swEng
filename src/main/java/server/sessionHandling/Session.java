package server.sessionHandling;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Session extends Serializable, Remote {
    boolean isOver() throws RemoteException;
    Iterable<String> getMessage() throws RemoteException;
    void submitCommand(String cmd) throws RemoteException;
    boolean stateChanged() throws RemoteException;
}
