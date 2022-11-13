package com.robotdebris.ncaaps2scheduler;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

public class ErrorMessage {

    private HttpStatus statusCode;
    private String message;
    private String description;

    public ErrorMessage(HttpStatus statusCode, String message, String description) {
        this.statusCode = statusCode;
        this.message = message;
        this.description = description;
    }
}
