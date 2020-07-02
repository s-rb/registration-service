package ru.list.surkovr.skblab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> {

    private T data;
    private MessageId id;

    public Message(T data) {
        this.data = data;
    }
}
