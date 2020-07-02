package ru.list.surkovr.skblab.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
public class RegistrationRequestDto {

    @NonNull
    private String login;
    @NonNull
    private String password;
    @NonNull
    private String email;
    @NonNull
    private String lastname;
    @NonNull
    private String firstname;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String middlename;
}
