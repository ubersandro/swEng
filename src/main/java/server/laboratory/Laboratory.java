package server.laboratory;


import server.sessionHandling.Session;
import server.examsHelper.ConcreteExamsHelper;
import server.laboratory.credentialsHelper.CredentialsHelperIF;
import server.laboratory.credentialsHelper.InvalidIdentifierException;
import utils.dbManagement.DBMS;
import server.examsHelper.ExamsHelper;
import server.sessionHandling.AdminSession;
import server.sessionHandling.StudentSession;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;

/**
 * Questo tipo enumerato descrive tutte le possibili istanze di CredentialsHelper. L'oggetto è preposto a coadiuvare il sistema nel riconoscimento
 * e nella validazione degli utenti che si loggano. Il comportamento di simili oggetti può facilmente essere modificato introducendo nuove istanze-implementazioni
 * del tipo enumerato.
 */


public final class Laboratory implements StudentEndpoint, AdminEndpoint {
    private final ExamsHelper helper = ConcreteExamsHelper.SINGLETON;

    enum CredHelper implements CredentialsHelperIF {
        TEST_HELPER {//implementazione naive per scopi di testing
            @Override
            public boolean IDCheck(LocalDate d, int ID, boolean isAStudent) {
                if(isAStudent){/*STUDENT CHECKS*/
                    if(ID!=1) return false ; //solo ID == 1 può loggarsi -> TEST
                }
                else{/*ADMIN CHECKS...*/
                    if(ID!=0) return false; //solo ID == 0 può loggarsi
                }
                return LocalDate.now().compareTo(d) <= 0; //condition sine qua non
            }
        }
    }

    public static void main(String[] args){
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        System.out.println("Security Manager ok...");
        Laboratory l = new Laboratory();


        System.out.println("LAB CREATED");
        try {
            Registry registry = LocateRegistry.getRegistry();
            System.out.println("Registry ok...");
            StudentEndpoint uep = (StudentEndpoint) UnicastRemoteObject.exportObject(l, 0);
            registry.rebind("LabUserEndPoint", uep);
            System.out.println("Publishing LabUserEndpoint ...");
            AdminEndpoint aep = l;
            registry.rebind("LabAdminEndpoint", aep);
            System.out.println("Publishing LabAdminEndpoint ...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Session adminLogin(int ID, LocalDate d) throws RemoteException, InvalidIdentifierException {
        //OPZIONALE check per identificare un amministratore.
        if (!CredHelper.TEST_HELPER.IDCheck(d, ID, false)){
            throw new InvalidIdentifierException(); // implementazioni naive funzionali al testing
        }
        System.out.println("L'admin con ID " + ID + " ha eseguito il login.");
        return new AdminSession(helper, ID);
    }

    @Override
    public Session studentLogin(int ID, LocalDate d, int mat) throws RemoteException, InvalidIdentifierException {
        if (!CredHelper.TEST_HELPER.IDCheck(d, ID, true) || DBMS.TEST_DB.studentLookup(mat) == null){
            throw new InvalidIdentifierException();
        }
        return new StudentSession(helper, ID, mat);
    }
}
