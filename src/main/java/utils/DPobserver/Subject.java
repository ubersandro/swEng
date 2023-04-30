package utils.DPobserver;

import java.util.LinkedList;
import java.util.List;

/**
 * Il linguaggio Java permette di definire costanti nelle interfacce -> in questo caso consente di risolvere
 * agevolemente il problema dell'implementazione di un oggetto Subject Singleton (di cui esiste una sola istanza),
 * cioè dell'oggetto che concretizza l'interfaccia ExamsHelper mediante l'uso di un tipo enumerato (i tipi enumerati
 * non possono estendere classi astratte o concrete, solo implementare interfacce).
 */
public interface Subject {
    //List<Observer> listeners = new LinkedList<>();// viola l'incapsulamento! implementazione "alternativa" del design pattern
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
}
