package server.laboratory.credentialsHelper;

import java.time.LocalDate;

/**
 * Funge da contratto per gli oggetti CredentialsHelper, che supportano il riconoscimento degli utenti. E' un'interfaccia funzionale.
 */
public interface CredentialsHelperIF {
    boolean IDCheck(LocalDate d, int ID, boolean isAStudent);
}
