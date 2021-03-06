package ga.fundamental.revolut.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super();
    }

    public AccountNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Account not found in database";
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
