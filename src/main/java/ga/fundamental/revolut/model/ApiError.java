package ga.fundamental.revolut.model;

import lombok.Data;

@Data
public class ApiError {
    private final String error;

    public static ApiError of(Throwable t) {
        return new ApiError(t.getMessage());
    }
}
