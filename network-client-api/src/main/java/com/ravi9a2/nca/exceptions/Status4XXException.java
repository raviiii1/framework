package com.ravi9a2.nca.exceptions;

/**
 * All the 4XX exceptions should be wrapped in {@code Status4XXException}
 *
 * @author raviiii1
 */
public class Status4XXException extends NetworkClientException {

    public Status4XXException(String message, int statusCode) {
        super(message, statusCode);
    }
}
