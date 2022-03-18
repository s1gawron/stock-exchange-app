package pl.eizodev.app.shared;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonPropertyOrder({ "timestamp", "code", "error", "message", "URI" })
@JsonDeserialize(builder = ErrorResponse.ErrorResponseBuilder.class)
public class ErrorResponse {

    private final String timestamp;

    private final int code;

    private final String error;

    private final String message;

    private final String URI;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ErrorResponseBuilder {

    }

}
