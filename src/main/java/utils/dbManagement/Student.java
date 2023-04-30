package utils.dbManagement;

/**
 * Rappresentazione PARZIALE di uno studente.
 */
public final class Student extends User{
    private final int mat ;

    @SuppressWarnings("unused")
    public int getMat() {
        return mat;
    }

    public Student(String CF, int ID, int m) {
        super(CF, ID);
        mat = m;
    }

    @Override
    public String toString() {
        return "Studente{" +
                "mat=" + mat +
                '}';
    }
}
