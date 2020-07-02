package ru.list.surkovr.skblab.services.interfaces;

import java.util.List;

public interface RegistrationService {

    List<String> register(String email, String login, String password,
                          String firstname, String lastname, String middlename);
}
