package com.robotdebris.ncaaps2scheduler;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

public class SchedulerError {

    private HttpStatus status;
    private String message;
    private List<String> errors;

    public SchedulerError(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public SchedulerError(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }

}
