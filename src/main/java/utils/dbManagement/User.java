package utils.dbManagement;

import java.io.Serializable;

/**
 * Rappresentazione PARZIALE di un utente generico.
 * Può essere ulteriormente raffinata per rappresentare uno studente o un admin.
 */
public class User implements Serializable {
    private final String CF;
    private final int ID; //document ID

    //private String name, surname,address, city, zipCode,telephone; -> punto di estensione

    public User(String CF, int ID) {
        this.CF = CF;
        this.ID = ID;
    }

    public String getCF() {
        return CF;
    }

    public int getID() {
        return ID;
    }
}
