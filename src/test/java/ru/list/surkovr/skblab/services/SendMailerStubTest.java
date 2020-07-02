package ru.list.surkovr.skblab.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import ru.list.surkovr.skblab.model.EmailAddress;
import ru.list.surkovr.skblab.model.EmailContent;
import ru.list.surkovr.skblab.model.Message;

import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
// информируем PowerMock что придется вносить изменения в класс LoggerFactory
@PrepareForTest({LoggerFactory.class})
public class SendMailerStubTest {

    public static final int TIMEOUT_TO_TEST_RECEIVE = 1000;
    // фейковый логгер
    private static Logger logger = mock(Logger.class);
    @InjectMocks
    private SendMailerStub sendMailer;

    // переопределна работа метода LoggerFactory.getLogger, при вызове всегда вернет фейк логгер
    static {
        PowerMockito.spy(LoggerFactory.class);
        try {
            PowerMockito.doReturn(logger).when(LoggerFactory.class, "getLogger",
                    any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testReceive_success() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        EmailAddress toAddress = new EmailAddress("mail@mail.ru");
        EmailContent content = new EmailContent("some content here");
        try {
            boolean isThreadWorkedFine = false;
            Future<Boolean> f = pool.submit(() -> {
                try {
                    sendMailer.sendMail(toAddress, content);
                } catch (TimeoutException e) {
                    return false;
                }
                return true;
            });
            isThreadWorkedFine = f.get(TIMEOUT_TO_TEST_RECEIVE, TimeUnit.MILLISECONDS);
            if (!isThreadWorkedFine) throw new TimeoutException("Timeout!");
        } catch (TimeoutException e) {
            exceptionRule.expect(TimeoutException.class);
            exceptionRule.expectMessage("Timeout!");
            return;
        }
        Mockito.verify(logger, Mockito.times(1))
                .info("Message sent to {}, body {}", toAddress, content);
    }
}
