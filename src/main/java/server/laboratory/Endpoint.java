package server.laboratory;


import java.io.Serializable;
import java.rmi.Remote;

/**
 * Marker interface che identifica un endpoint per gli utenti.
 */
public interface Endpoint extends Serializable, Remote {}
