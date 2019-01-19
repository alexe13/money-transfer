package ga.fundamental.revolut.exception;

public class MalformedRequestException extends RuntimeException {

    public MalformedRequestException() {
        super();
    }

    public MalformedRequestException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Malformed request";
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
