package ru.list.surkovr.skblab.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.list.surkovr.skblab.dto.requests.RegistrationRequestDto;
import ru.list.surkovr.skblab.dto.responses.BaseResponseDto;
import ru.list.surkovr.skblab.dto.responses.ErrorsResponseDto;
import ru.list.surkovr.skblab.services.interfaces.RegistrationService;

import java.util.List;

@RestController
@RequestMapping("/api/auth/")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto> register(@RequestBody RegistrationRequestDto requestDto) {
        String email = requestDto.getEmail();
        String login = requestDto.getLogin();
        String password = requestDto.getPassword();
        String firstname = requestDto.getFirstname();
        String lastname = requestDto.getLastname();
        String middlename = requestDto.getMiddlename();

        List<String> errors = registrationService.register(email, login, password,
                firstname, lastname, middlename);
        if (errors.isEmpty()) {
            return ResponseEntity.ok(new BaseResponseDto(true));
        } else {
            ErrorsResponseDto errorsResponseDto = new ErrorsResponseDto(false);
            errorsResponseDto.setErrors(errors);
            return ResponseEntity.badRequest().body(errorsResponseDto);
        }
    }
}
