package ru.list.surkovr.skblab.services.interfaces;

import ru.list.surkovr.skblab.model.EmailAddress;
import ru.list.surkovr.skblab.model.EmailContent;

import java.util.concurrent.TimeoutException;

public interface SendMailer {

    void sendMail(EmailAddress toAddress, EmailContent messageBody) throws TimeoutException;
}
