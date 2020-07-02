package ru.list.surkovr.skblab;

import ru.list.surkovr.skblab.dto.requests.RegistrationRequestDto;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.entities.User;

public class TestUtils {

    public static String contentEmailAfterSuccessReg = "Вы успшено зарегистрированы в нашем сервисе!";

    public static RegistrationRequestDto getRegisterRequest() {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setEmail("new@mail.ru");
        request.setLogin("new_login");
        request.setPassword("New_password7");
        request.setFirstname("Ivan");
        request.setLastname("Ivanov");
        request.setMiddlename("Ivanovich");
        return request;
    }

    public static Message<String> getMessage(String email, String login, String password,
                                             String firstname, String lastname, String middlename) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"action\":").append("\"register\"").append(",")
                .append("\"login\":").append(login).append(",")
                .append("\"email\":").append(email).append(",")
                .append("\"password\":").append(password).append(",")
                .append("\"firstname\":").append(firstname).append(",")
                .append("\"lastname\":").append(lastname);
        if (middlename != null && !middlename.isBlank()) {
            sb.append(",")
                    .append("\"middlename\":").append(middlename);
        }
        sb.append("}");
        return new Message<String>(sb.toString());
    }

    public static User createUser(String email, String login, String password,
                                  String firstname, String lastname, String middlename) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setPassword(password);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        if (middlename != null && !middlename.isBlank()) user.setMiddlename(middlename);
        return user;
    }
}