package com.ravi9a2.nca.exceptions;

/**
 * All the 5XX exceptions should be wrapped in {@code Status5XXException}
 *
 * @author raviiii1
 */
public class Status5XXException extends NetworkClientException {

    public Status5XXException(String message, int statusCode) {
        super(message, statusCode);
    }
}
