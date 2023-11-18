package com.auth.authserver2.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ResponseMessage {

    private boolean isSuccessful;
    private String msg;

}
