package ru.list.surkovr.skblab.services.interfaces;

import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;

import java.util.concurrent.TimeoutException;

public interface MessagingService {

    // Отправка сообщения в шину
    // Возвращает идентификатор отправленного сообщения (correlationId)
    <T> MessageId send(Message<T> msg);

    // Встает на ожидание ответа по сообщению с messageId
    // Иногда по тайм-ауту кидает исключение
    // MessageId - идентификатор сообщения на которое ждем ответ
    // messageType - тип сообщения к которому необходимо привести ответ
    <T> Message<T> receive(MessageId messageId, Class<T> messageType) throws TimeoutException;
}
