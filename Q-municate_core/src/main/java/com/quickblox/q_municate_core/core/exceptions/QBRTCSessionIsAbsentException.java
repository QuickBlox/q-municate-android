package com.quickblox.q_municate_core.core.exceptions;

/**
 * Created by PC on 09.06.2015.
 */
public class QBRTCSessionIsAbsentException extends Exception {

    private static final String DEFAULT_MESSAGE = "You've try to make method call on null QBRTCSession reference";

    @Override
    public String getMessage() {
        if (super.getMessage() == null) {
            return DEFAULT_MESSAGE;
        } else {
            return super.getMessage();
        }
    }
}
