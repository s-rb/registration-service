package ru.list.surkovr.skblab.services;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;
import ru.list.surkovr.skblab.services.interfaces.MessagingService;
import ru.list.surkovr.skblab.services.interfaces.OuterCheckingService;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class MessagingServiceImpl implements MessagingService {

    private OuterCheckingService outerCheckingService;
    private final Map<MessageId, Boolean> msgCheckMap;

    @Autowired
    public MessagingServiceImpl(OuterCheckingService outerCheckingService) {
        this.outerCheckingService = outerCheckingService;
        msgCheckMap = new HashMap<>();
    }

    /**
     * Отправка сообщения в шину.
     *
     * @param msg сообщение для отправки.
     * @return идентификатор отправленного сообщения (correlationId)
     */
    @Override
    public <T> MessageId send(Message<T> msg) {
        // Message Service получает сообщение от сервиса по регистрации, присваивает Id
        // Отправляет во внешнюю службу для проверки. Получаем ответ, сохраняем его в Map и возвращаем по запросу
        // Возвращает Id в сервис, по которому сервис будет получать ответное сообщение
        MessageId id = null;
        try {
            boolean isTrue = outerCheckingService.checkMessage(msg);
            id = new MessageId(UUID.randomUUID());
            msgCheckMap.put(id, isTrue);
        } catch (Exception e) {
            // Если внешний сервис недоступен, то Id - null. Можно написать в лог
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Встает на ожидание ответа по сообщению с messageId.
     * <p>
     * Редко, но может кинуть исключение по таймауту.
     *
     * @param messageId   идентификатор сообщения, на которое ждем ответ.
     * @param messageType тип сообщения, к которому необходимо привести ответ.
     * @return Тело ответа.
     */
    @Override
    public <T> Message<T> receive(MessageId messageId, Class<T> messageType) throws TimeoutException {
        if (shouldThrowTimeout()) {
            sleep();
            throw new TimeoutException("Timeout!");
        }
        if (shouldSleep()) {
            sleep();
        }
        // Все проверили во внешнем сервисе, если на сообщение был ответ, он сохранен в Map
        // Создаем ответное сообщение, только случай с Boolean
        Message<Boolean> msg = null;
        if (messageType == Boolean.class) {
            msg = new Message<Boolean>(msgCheckMap.get(messageId));
            msg.setId(messageId);
        }
        return (Message<T>) msg;
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
