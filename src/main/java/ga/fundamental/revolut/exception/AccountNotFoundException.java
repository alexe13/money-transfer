package ga.fundamental.revolut.exception;

public class AccountNotFoundException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Account not found in database";
    }

    @Override
    public String toString() {
        return "Account not found in database";
    }
}
