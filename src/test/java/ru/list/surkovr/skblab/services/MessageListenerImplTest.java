package ru.list.surkovr.skblab.services;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.LoggerFactory;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
// информируем PowerMock что придется вносить изменения в класс LoggerFactory
@PrepareForTest({LoggerFactory.class})
public class MessageListenerImplTest {

    @InjectMocks
    private MessageListenerImpl messageListener;
    // фейковый логгер
    private static Logger logger = mock(Logger.class);

    // переопределна работа метода LoggerFactory.getLogger
    // при вызове всегда вернет фейк логгер
    static {
        PowerMockito.spy(LoggerFactory.class);
        try {
            PowerMockito.doReturn(logger).when(LoggerFactory.class, "getLogger",
                    any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleMessage() {
        var id = new MessageId(UUID.randomUUID());
        var msg = new Message<Boolean>(true);
        msg.setId(id);
        messageListener.handleMessage(msg);
        Mockito.verify(logger, Mockito.times(1))
                .info("MessageListner handles message with id {}", id);
    }
}
