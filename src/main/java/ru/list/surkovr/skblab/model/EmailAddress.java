package ru.list.surkovr.skblab.model;

import lombok.Data;

@Data
public class EmailAddress {

    private String email;

    public EmailAddress(String email) {
        this.email = email;
    }
}
