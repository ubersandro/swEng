package server.laboratory.credentialsHelper;

public class InvalidIdentifierException extends Exception{
    @Override
    public String getMessage() {
        return "Le credenziali inserite sono errate!";
    }
}
