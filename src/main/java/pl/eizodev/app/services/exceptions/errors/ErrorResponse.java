package pl.eizodev.app.services.exceptions.errors;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@AllArgsConstructor
@ToString
public class ErrorResponse {

    private final Instant timeStamp;
    private final int code;
    private final String error;
    private final String message;
    private final String URI;

    public ErrorResponse(int code, String error, String message, String URI) {
        this.timeStamp = Instant.now();
        this.code = code;
        this.error = error;
        this.message = message;
        this.URI = URI;
    }
}
