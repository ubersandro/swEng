package consolle;

import server.laboratory.credentialsHelper.InvalidIdentifierException;

public interface Consolle {
    void welcome();
    void login() throws InvalidIdentifierException;
    void quit();
    void logout();
}
