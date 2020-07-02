package ru.list.surkovr.skblab.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.list.surkovr.skblab.model.EmailAddress;
import ru.list.surkovr.skblab.model.EmailContent;
import ru.list.surkovr.skblab.services.interfaces.SendMailer;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class SendMailerStub implements SendMailer {
    @Override
    public void sendMail(EmailAddress toAddress, EmailContent messageBody) throws TimeoutException {
        if (shouldThrowTimeout()) {
            sleep();
            throw new TimeoutException("Timeout!");
        }
        if (shouldSleep()) {
            sleep();
        }
        log.info("Message sent to {}, body {}", toAddress, messageBody);
    }

    @SneakyThrows
    private static void sleep() {
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }

    private static boolean shouldSleep() {
        return new Random().nextInt(10) == 1;
    }

    private static boolean shouldThrowTimeout() {
        return new Random().nextInt(10) == 1;
    }
}
