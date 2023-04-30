import org.junit.jupiter.api.*;
import server.laboratory.Laboratory;
import server.laboratory.credentialsHelper.InvalidIdentifierException;
import java.time.LocalDate;

/**
 * Per testare il modulo Laboratory è stato necessario fornire implementazioni di testing (stubs) del DBManager e di CredHelper.
 * PRINCIPI DI TESTING :
 * 1) COMPLETE COVERAGE: scelti campioni che si confanno ai vincoli di più sottinsiemi ad
 *  intersezione nulla (ma non solo, anche combinazioni di casi dai vai sottinsiemi presi in
 *  esame). Domini presi in esame D1 = DATE NON VALIDE , D2 = ID NON VALIDI, D3 = MATRICOLE NON VALIDE
 * 2) BLACK BOX TESTING: basato su ciò che il sistema dovrebbe fare, non sul codice che implementa i servizi.
 * 3) I campioni selezionati sono rappresentativi, il comportamento del sistema a diverse combo con caratteristiche
 * analoghe (presi dallo stesso sottinsieme di possibili input) non cambia.
 * 4) CRITERIO : Coverage Criterion.
 * 5) I test sono derivati dalle specifiche e si basano sulle postcondizioni dei metodi della classe Laboratory.
 * Un sistema laboratorio deve permettere il login di un utente noto, sia esso studente o amministratore.
 */
public class LaboratoryTest {
    private static final int TIMEOUT_DEFAULT = 3000;
    private Laboratory l;

    @BeforeEach
    void initialize(){
        l = new Laboratory();
    }

    //premessa = nel sistema si può loggare solo lo studente con ID = 1 , mat = 1 e doc non scaduto
    @Test @Timeout(2000)
    void StudentLoginTest() {
        Assertions.assertDoesNotThrow(()->l.studentLogin(1, LocalDate.now().plusDays(1),1),
                "Login studente fallito");
    }

    @Test @Timeout(TIMEOUT_DEFAULT)
    void AdminLoginTest(){
        Assertions.assertDoesNotThrow(()->{l.studentLogin(1, LocalDate.now().plusDays(1),1);},
                "Login admin fallito");
    }

    @Test
    void StudentLoginFailTest_MAT() {
        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.studentLogin(1, LocalDate.now().plusDays(1), -1); //matricola sbagliata
        }, "USER NOT LOGGED");
    }

    @Test
    void StudentLoginFailTest_DOC() {
        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.studentLogin(1, LocalDate.now().minusDays(1), 1); //documento scaduto
        }, "USER NOT LOGGED");
    }
    @Test
    void StudentLoginFailTest_ID() {
        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.studentLogin(-1, LocalDate.now().plusDays(1), 1); //ID non valido
        }, "USER NOT LOGGED");
    }
    @Test
    void StudentLoginFailTest_ID_MAT() {
        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.studentLogin(-1, LocalDate.now().plusDays(1), -1); //id sbagliato,doc valido, matricola non valida
        }, "USER NOT LOGGED");
    }
    @Test
    void StudentLoginFailTest_ID_DOC() {

        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.studentLogin(-1, LocalDate.now().minusDays(1), 1); //id sbagliato, doc scaduto, matricola valida
        }, "USER NOT LOGGED");
    }
    @Test
    void StudentLoginFailTest_MAT_DOC() {

        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.studentLogin(1, LocalDate.now().minusDays(1), -1); //id valido, doc scaduto, matricola non valida
        }, "USER NOT LOGGED");
    }
    @Test
    void StudentLoginFailTest_ID_MAT_DOC() {
        Assertions.assertThrows(InvalidIdentifierException.class, ()->{
            l.studentLogin(-1, LocalDate.now().minusDays(1),-1); //id non valido , doc scaduto , matricola non valida
        },"USER NOT LOGGED");
    }

    //premessa = nel sistema si può loggare solo l'admin con ID = 0 e doc non scaduto

    @Test
    void AdminLoginFailTest_DOC() {
        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.adminLogin(0, LocalDate.now().minusDays(1)); //documento scaduto
        }, "USER NOT LOGGED");
    }
    @Test
    void AdminLoginFailTest_ID() {
        Assertions.assertThrows(InvalidIdentifierException.class, () -> {
            l.adminLogin(1, LocalDate.now().plusDays(1)); //ID sbagliato
        }, "USER NOT LOGGED");
    }
    @Test
    void AdminLoginFailTest_ID_DOC() {
        Assertions.assertThrows(InvalidIdentifierException.class, ()->{
            l.adminLogin(1, LocalDate.now().minusDays(1)); //id sbagliato E documento scaduto
        },"USER NOT LOGGED");
    }

}
