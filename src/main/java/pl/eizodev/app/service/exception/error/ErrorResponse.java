package pl.eizodev.app.service.exception.error;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.time.Instant;

@Getter
@JsonPropertyOrder({"timestamp", "code", "error", "message", "URI"})
public class ErrorResponse {

    private final Instant timestamp;
    private final int code;
    private final String error;
    private final String message;
    private final String URI;

    public ErrorResponse(int code, String error, String message, String URI) {
        this.timestamp = Instant.now();
        this.code = code;
        this.error = error;
        this.message = message;
        this.URI = URI;
    }
}
