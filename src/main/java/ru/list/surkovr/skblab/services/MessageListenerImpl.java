package ru.list.surkovr.skblab.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.services.interfaces.MessageListener;

@Component
@Slf4j
public class MessageListenerImpl<Boolean> implements MessageListener<Boolean> {

    // Опциональный интерфейс листнеров
    @Override
    public void handleMessage(Message<Boolean> incomingMessage) {
        // Используется для выполнения работы по результатам проверки
        if (incomingMessage.getData() == null) return;
        if (incomingMessage.getData().equals(true)) {
            // Если ОК, то выполняем полезную работу
            log.info("MessageListner handles message with id {}", incomingMessage.getId());
        }
    }
}
