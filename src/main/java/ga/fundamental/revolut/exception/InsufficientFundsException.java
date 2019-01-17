package ga.fundamental.revolut.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super();
    }

    public InsufficientFundsException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Not enough funds to perform operation";
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
