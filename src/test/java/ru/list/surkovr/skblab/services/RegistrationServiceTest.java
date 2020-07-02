package ru.list.surkovr.skblab.services;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.list.surkovr.skblab.TestUtils;
import ru.list.surkovr.skblab.dto.requests.RegistrationRequestDto;
import ru.list.surkovr.skblab.model.EmailAddress;
import ru.list.surkovr.skblab.model.EmailContent;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;
import ru.list.surkovr.skblab.model.entities.User;
import ru.list.surkovr.skblab.repositories.UserRepository;
import ru.list.surkovr.skblab.services.interfaces.MessageListener;
import ru.list.surkovr.skblab.services.interfaces.MessagingService;
import ru.list.surkovr.skblab.services.interfaces.SendMailer;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.list.surkovr.skblab.TestUtils.*;

@RunWith(SpringRunner.class)
public class RegistrationServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    private MessagingService messagingService;
    @Mock
    private SendMailer sendMailer;
    @Mock
    private MessageListener messageListener;
    @InjectMocks
    RegistrationServiceImpl registrationService;

    @Test
    public void testRegisterUserSimple_success() throws TimeoutException {
        var request = getRegisterRequest();
        var userToRegister = getUserToRegister(request);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());

        callMethodRegister(List.of(), new MessageId(UUID.randomUUID()), new Message<Boolean>(true),
                null, null, userToRegister, savedUser, false);
    }

    @Test
    public void testRegisterUser_emptyParams() throws TimeoutException {
        var login = "  ";
        var email = " ";
        var password = "";
        var firstname = " ";
        var lastname = " ";
        var middlename = "   ";
        User userToRegister = createUser(email.toLowerCase(), login, password, firstname, lastname, middlename);
        List<String> expectedErrors = List.of("Логин не заполнен",
                "Email не заполнен", "Имя не заполнено", "Фамилия не заполнена",
                "Пароль не введен или не соответствует требованиям безопасности: " +
                        "должен состоять из >5 символов, содержать буквы в разном регистре и цифры");
        callMethodRegister(expectedErrors, null, null, null,
                null, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_wrongParams() throws TimeoutException {
        var login = "login"; // existed
        var email = "mail@mail.ru"; // existed
        var password = "pass"; // weak
        var firstname = "kmj21"; // contains numbers
        var lastname = "klj12"; // contains numbers
        var middlename = "%$#dsd"; // contains symbols
        User userToRegister = createUser(email.toLowerCase(), login, password, firstname, lastname, middlename);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        List<String> expectedErrors = List.of("Логин уже зарегистрирован",
                "Email уже зарегистрирован", "Имя содержит недопустимые символы",
                "Фамилия содержит недопустимые символы", "Отчество содержит недопустимые символы",
                "Пароль не введен или не соответствует требованиям безопасности: " +
                        "должен состоять из >5 символов, содержать буквы в разном регистре и цифры");
        callMethodRegister(expectedErrors, null, null, savedUser,
                savedUser, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_wrongEmailFormat() throws TimeoutException {
        var login = "login";
        var email = "mail_ru";
        var password = "Password7";
        var firstname = "Petrr";
        var lastname = "Petrov";
        String middlename = null;
        User userToRegister = createUser(email.toLowerCase(), login, password, firstname, lastname, middlename);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        List<String> expectedErrors = List.of("Формат email не соответствует принятому: " +
                "\"[address]@[domain2].[domain1]\"");
        callMethodRegister(expectedErrors, null, null, null,
                null, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_msgServiceSleepsWhenSend() throws TimeoutException {
        var request = getRegisterRequest();
        var userToRegister = getUserToRegister(request);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        List<String> expectedErrors = List.of("Регистрация невозможна. Служба проверки недоступна. Попробуйте позже");
        callMethodRegister(expectedErrors, null, null, null,
                null, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_msgServiceSleepsWhenReceive() throws TimeoutException {
        var request = getRegisterRequest();
        var userToRegister = getUserToRegister(request);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        List<String> expectedErrors = List.of("Таймаут доступа к службе проверки. Попробуйте позже");
        callMethodRegister(expectedErrors, new MessageId(UUID.randomUUID()), null, null,
                null, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_answerMsgDataNull() throws TimeoutException {
        var request = getRegisterRequest();
        var userToRegister = getUserToRegister(request);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        List<String> expectedErrors = List.of("Не удалось получить ответ от службы регистрации");
        callMethodRegister(expectedErrors, new MessageId(UUID.randomUUID()), new Message<Boolean>(null), null,
                null, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_registerRestricted() throws TimeoutException {
        var request = getRegisterRequest();
        var userToRegister = getUserToRegister(request);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        List<String> expectedErrors = List.of("Регистрация с указанными параметрами запрещена");
        callMethodRegister(expectedErrors, new MessageId(UUID.randomUUID()), new Message<Boolean>(false), null,
                null, userToRegister, null, false);
    }

    @Test
    public void testRegisterUser_sendMailThrowsTimeout() throws TimeoutException {
        var request = getRegisterRequest();
        var userToRegister = getUserToRegister(request);
        var savedUser = createUser(userToRegister.getEmail(), userToRegister.getLogin(),
                userToRegister.getPassword(), userToRegister.getFirstname(), userToRegister.getLastname(),
                userToRegister.getMiddlename());
        savedUser.setId(new Random().nextLong());
        var id = new MessageId(UUID.randomUUID());
        List<String> expectedErrors = List.of("Служба отправки сообщений недоступна");
        callMethodRegister(expectedErrors, id, new Message<Boolean>(true), null,
                null, userToRegister, savedUser, true);
    }

    private User getUserToRegister(RegistrationRequestDto request) {
        var login = request.getLogin();
        var email = request.getEmail();
        var password = request.getPassword();
        var firstname = request.getFirstname();
        var lastname = request.getLastname();
        var middlename = request.getMiddlename();
        return createUser(email.toLowerCase(), login, password, firstname, lastname, middlename);
    }

    public void callMethodRegister(List<String> expected, MessageId idReturnedByMsgSrvc,
                                   Message<Boolean> msgReturnedByMsgSrvc, User userFoundByLogin,
                                   User userFoundByEmail, User userToRegister, User savedUser,
                                   boolean isSendMailThrowsException) throws TimeoutException {
        var registerMessage = getMessage(userToRegister.getEmail().toLowerCase(),
                userToRegister.getLogin(), userToRegister.getPassword(), userToRegister.getFirstname(),
                userToRegister.getLastname(), userToRegister.getMiddlename());

        var toAddress = new EmailAddress(userToRegister.getEmail());
        var emailContent = new EmailContent(contentEmailAfterSuccessReg);

        given(userRepository.findUserByLogin(userToRegister.getLogin())).willReturn(userFoundByLogin);
        given(userRepository.findUserByEmail(userToRegister.getEmail())).willReturn(userFoundByEmail);

        given(messagingService.send(registerMessage)).willReturn(idReturnedByMsgSrvc);
        var id = messagingService.send(registerMessage);
        given(messagingService.receive(id, Boolean.class)).willReturn(msgReturnedByMsgSrvc);
        given(userRepository.save(userToRegister)).willReturn(savedUser);
        if (isSendMailThrowsException) {
            doThrow(TimeoutException.class).when(sendMailer).sendMail(any(), any());
        } else {
            doNothing().when(sendMailer).sendMail(toAddress, emailContent);
        }

        var actual = registrationService.register(userToRegister.getEmail().toLowerCase(),
                userToRegister.getLogin(), userToRegister.getPassword(),
                userToRegister.getFirstname(), userToRegister.getLastname(), userToRegister.getMiddlename());

        assertNotNull(actual);
        assertThat(actual, Is.is(expected));
    }
}
