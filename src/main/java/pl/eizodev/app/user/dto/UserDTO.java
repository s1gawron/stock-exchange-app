package pl.eizodev.app.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@JsonDeserialize(builder = UserDTO.UserDTOBuilder.class)
public class UserDTO {

    private final String username;

    private final String email;

    private final UserWalletDTO userWallet;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDTOBuilder {

    }

}
