package is.fivefivefive.alloyasg.exceptions;

public class UnsupportedConstantException extends Exception {
    public UnsupportedConstantException(String name) {
        super("Constant " + name + " is not supported.");
    }
}
