package ee.taltech.ite4120.landuse.register;

/** The restrictions register could not answer — transport failure or upstream fault. Maps to 502. */
public class RestrictionRegisterException extends RuntimeException {

    public RestrictionRegisterException(String message) {
        super(message);
    }

    public RestrictionRegisterException(String message, Throwable cause) {
        super(message, cause);
    }
}
