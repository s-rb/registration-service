package ru.list.surkovr.skblab.services;

import org.springframework.stereotype.Service;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.services.interfaces.OuterCheckingService;

import java.util.Random;

@Service
public class OuterCheckingServiceImpl<T> implements OuterCheckingService<T> {

    @Override
    public boolean checkMessage(Message<T> msg) {
        // Что-то делаем с msg. Проверяем регистрировать или нет.
        return new Random().nextBoolean();
    }
}