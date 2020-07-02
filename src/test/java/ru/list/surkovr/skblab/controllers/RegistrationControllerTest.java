package ru.list.surkovr.skblab.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.list.surkovr.skblab.services.interfaces.RegistrationService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.list.surkovr.skblab.TestUtils.getRegisterRequest;

@RunWith(SpringRunner.class)
@WebMvcTest(RegistrationController.class)
public class RegistrationControllerTest {

    public static final String URL_API_AUTH = "/api/auth/";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Test
    public void testRegisterUser_success() throws Exception {
        var request = getRegisterRequest();

        var email = request.getEmail();
        var login = request.getLogin();
        var password = request.getPassword();
        var firstName = request.getFirstname();
        var lastName = request.getLastname();
        var middleName = request.getMiddlename();
        ObjectMapper mapper = new ObjectMapper();

        var success = true;
        given(registrationService.register(email, login, password, firstName, lastName, middleName))
                .willReturn(List.of());
        mockMvc.perform(post(URL_API_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", Is.is(success)));
    }

    @Test
    public void testRegisterUser_someErrors() throws Exception {
        var request = getRegisterRequest();

        var email = request.getEmail();
        var login = request.getLogin();
        var password = request.getPassword();
        var firstName = request.getFirstname();
        var lastName = request.getLastname();
        var middleName = request.getMiddlename();
        ObjectMapper mapper = new ObjectMapper();

        var success = false;
        var error1 = "Error 1 has occured";
        var error2 = "Error 2 has occured";
        given(registrationService.register(email, login, password, firstName, lastName, middleName))
                .willReturn(List.of(error1, error2));
        mockMvc.perform(post(URL_API_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", Is.is(success)))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0]", Is.is(error1)))
                .andExpect(jsonPath("$.errors[1]", Is.is(error2)));
    }
}
