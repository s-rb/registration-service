package ru.list.surkovr.skblab.model.entities;

import lombok.Data;

@Data
public class Message<T> {

    private T data;
    private MessageId id;

    public Message(T data) {
        this.data = data;
    }

    public Message(T data, MessageId id) {
        this.data = data;
        this.id = id;
    }
}
