package ru.list.surkovr.skblab.services.interfaces;

import ru.list.surkovr.skblab.model.Message;

public interface MessageListener<T> {

    // T - тип сообщения, который будем слушать
    void handleMessage(Message<T> incomingMessage);
}
