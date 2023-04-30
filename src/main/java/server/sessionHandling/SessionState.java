package server.sessionHandling;

/**
 * Visibile solo nel package.
 * @param <T>
 */
interface SessionState< T extends AbstractSession> {
    Iterable<String> entryAction(T s);
    void callback(String cmdCode, T session);
}
