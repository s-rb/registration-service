package ru.list.surkovr.skblab.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import ru.list.surkovr.skblab.model.EmailAddress;
import ru.list.surkovr.skblab.model.EmailContent;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;
import ru.list.surkovr.skblab.model.entities.User;
import ru.list.surkovr.skblab.repositories.UserRepository;
import ru.list.surkovr.skblab.services.interfaces.MessageListener;
import ru.list.surkovr.skblab.services.interfaces.MessagingService;
import ru.list.surkovr.skblab.services.interfaces.RegistrationService;
import ru.list.surkovr.skblab.services.interfaces.SendMailer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
@ComponentScan("ru.list.surkovr.skblab.services")
public class RegistrationServiceImpl implements RegistrationService {

    public String regexEmailValidation = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
    public String regexPasswordValidation = "^.{6,}$";
    public String regexNameValidation = "[A-Za-z\\s-]{1,}";
    public String contentEmailAfterSuccessReg = "Вы успшено зарегистрированы в нашем сервисе!";
    public int timeoutToWaitThread = 5000;
    public int sizeOfPool = 4;

    private final MessagingService messagingService;
    private final SendMailer sendMailer;
    private final UserRepository userRepository;
    private final MessageListener messageListener;
    private final ExecutorService pool = Executors.newWorkStealingPool(sizeOfPool);

    @Autowired
    public RegistrationServiceImpl(MessagingService messagingService,
                                   SendMailer sendMailer,
                                   UserRepository userRepository,
                                   MessageListener messageListener) {
        this.messagingService = messagingService;
        this.sendMailer = sendMailer;
        this.userRepository = userRepository;
        this.messageListener = messageListener;
    }

    @Override
    public List<String> register(String email, String login, String password,
                                 String firstname, String lastname, String middlename) {
        List<String> errors = getErrorsForInputData(email, login, password, firstname, lastname, middlename);
        if (!errors.isEmpty()) return errors;
        // Отправляем запрос на проверку во внешнюю систему. Сохраняем юзера в БД только после одобрения от внешней системы
        Message<String> registerMessage = createRegisterMessage(email.toLowerCase(), login, password,
                firstname, lastname, middlename);
        MessageId id;
        try {
            Future<MessageId> f = pool.submit(() -> {
                return messagingService.send(registerMessage);
            });
            id = f.get(timeoutToWaitThread, TimeUnit.MILLISECONDS);
            if (id == null) throw new InterruptedException("TIMEOUT!");
        } catch (Exception e) {
            e.printStackTrace();
            // messageService unavailable. Couldn't register! try again later
            errors.add("Регистрация невозможна. Служба проверки недоступна. Попробуйте позже");
            return errors;
        }

        // получаем ответ от службы проверки. True - ок, регистрируем
        Message<Boolean> answerMsg;
        try {
            Future<Message<Boolean>> f = pool.submit(() -> {
                return messagingService.receive(id, Boolean.class);
            });
            answerMsg = f.get(timeoutToWaitThread, TimeUnit.MILLISECONDS);
            if (answerMsg == null) throw new TimeoutException("TIMEOUT!");
        } catch (TimeoutException e) {   // Не удалось, время вышло
            errors.add("Таймаут доступа к службе проверки. Попробуйте позже");
            return errors;
        } catch (Exception e) {
            errors.add("Что-то пошло не так. Попробуйте позже");
            e.printStackTrace();
            return errors;
        }
        if (answerMsg.getData() == null) {
            errors.add("Не удалось получить ответ от службы регистрации");
            return errors;
        }
        // Опционально передаем результат в хэндлер
        messageListener.handleMessage(answerMsg);

        // Если ответ True (одобрен) - Регистрируем пользователя в нашей БД и отправляем ему Email
        if (!answerMsg.getData()) {
            errors.add("Регистрация с указанными параметрами запрещена");
            return errors;
        } else {
            User user = createUser(email.toLowerCase(), login, password, firstname, lastname, middlename);
            user = userRepository.save(user);
            log.info("Успешно зарегистрирован пользователь с id: {}, login: {}, email: {}",
                    user.getId(), user.getLogin(), user.getEmail());

            EmailAddress toAddress = new EmailAddress(email);
            EmailContent emailContent = new EmailContent(contentEmailAfterSuccessReg);
            try {
                sendMailer.sendMail(toAddress, emailContent);
            } catch (TimeoutException e) {
                e.printStackTrace();
                errors.add("Служба отправки сообщений недоступна");
                return errors;
            }
        }
        return errors;
    }

    private Message<String> createRegisterMessage(String email, String login, String password,
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

    private User createUser(String email, String login, String password,
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

    private List<String> getErrorsForInputData(String email, String login, String password,
                                               String firstname, String lastname, String middlename) {
        boolean isLoginValid = login != null && !login.isBlank();
        boolean isLoginNotExists = userRepository.findUserByLogin(login) == null;

        boolean isEmailNotEmpty = email != null && !email.isBlank();
        Boolean isEmailMatchesRegex = !isEmailNotEmpty ? null : email.matches(regexEmailValidation);
        Boolean isEmailNotExistsInDb = isEmailNotEmpty && isEmailMatchesRegex ?
                userRepository.findUserByEmail(email.toLowerCase()) == null : null;

        boolean isPasswordValid = password != null && !password.isBlank()
                && password.matches(regexPasswordValidation);

        boolean isFirstNameNotEmpty = firstname != null && !firstname.isBlank();
        Boolean isFirstNameContainsRightChars = isFirstNameNotEmpty ? firstname.matches(regexNameValidation) : null;

        boolean isLastNameNotEmpty = lastname != null && !lastname.isBlank();
        Boolean isLastNameContainsRightChars = isLastNameNotEmpty ? lastname.matches(regexNameValidation) : null;

        boolean isMiddleNameBlank = middlename == null || middlename.isBlank();
        boolean isMiddleNameContainsRightChars = !isMiddleNameBlank && middlename.matches(regexNameValidation);

        List<String> errorsList = new LinkedList<>();
        putErrorsToList(errorsList, isLoginValid, isLoginNotExists, isEmailNotEmpty, isEmailMatchesRegex,
                isEmailNotExistsInDb, isFirstNameNotEmpty, isFirstNameContainsRightChars,
                isLastNameNotEmpty, isLastNameContainsRightChars, isMiddleNameBlank, isMiddleNameContainsRightChars, isPasswordValid);
        return errorsList;
    }

    private void putErrorsToList(List<String> errorsList, boolean isLoginValid, boolean isLoginNotExists,
                                 boolean isEmailNotEmpty, Boolean isEmailMatchesRegex, Boolean isEmailNotExistsInDb,
                                 boolean isFirstNameNotEmpty, Boolean isFirstNameContainsRightChars,
                                 boolean isLastNameNotEmpty, Boolean isLastNameContainsRightChars,
                                 boolean isMiddleNameBlank, boolean isMiddleNameContainsRightChars,
                                 boolean isPasswordValid) {
        if (!isLoginValid) errorsList.add("Логин не заполнен");
        if (!isLoginNotExists) errorsList.add("Логин уже зарегистрирован");

        if (!isEmailNotEmpty) errorsList.add("Email не заполнен");
        if (isEmailMatchesRegex != null && !isEmailMatchesRegex)
            errorsList.add("Формат email не соответствует принятому: " +
                    "\"[address]@[domain2].[domain1]\"");
        if (isEmailNotExistsInDb != null && !isEmailNotExistsInDb) errorsList.add("Email уже зарегистрирован");

        if (!isFirstNameNotEmpty) errorsList.add("Имя не заполнено");
        if (isFirstNameContainsRightChars != null && !isFirstNameContainsRightChars)
            errorsList.add("Имя содержит недопустимые символы");

        if (!isLastNameNotEmpty) errorsList.add("Фамилия не заполнена");
        if (isLastNameContainsRightChars != null && !isLastNameContainsRightChars)
            errorsList.add("Фамилия содержит недопустимые символы");

        if (!isMiddleNameBlank && !isMiddleNameContainsRightChars)
            errorsList.add("Отчество содержит недопустимые символы");

        // Вывод сообщения с инструкцией какой должне быть пароль
        if (!isPasswordValid) errorsList.add("Пароль не введен или не соответствует требованиям безопасности: " +
                "должен состоять из >5 символов, содержать буквы в разном регистре и цифры");
    }
}