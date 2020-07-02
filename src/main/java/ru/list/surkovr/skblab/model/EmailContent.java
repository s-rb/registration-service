package ru.list.surkovr.skblab.model;

import lombok.Data;

@Data
public class EmailContent {

    private String content;

    public EmailContent(String content) {
        this.content = content;
    }
}
