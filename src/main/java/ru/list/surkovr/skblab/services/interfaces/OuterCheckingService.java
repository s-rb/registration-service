package ru.list.surkovr.skblab.services.interfaces;

import ru.list.surkovr.skblab.model.Message;

public interface OuterCheckingService<T> {

    boolean checkMessage(Message<T> msg);
}
