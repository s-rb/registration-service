package ru.list.surkovr.skblab.model;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageId {

    private UUID uuid;

    public MessageId(UUID uuid) {
        this.uuid = uuid;
    }
}