package com.ravi9a2.nca.exceptions;

/**
 * The type of any exception thrown by the implementations of this
 * networking-client should be either {@code ravi9a2ClientException}
 * or its children.
 *
 * @author raviiii1
 */
public class NetworkClientException extends RuntimeException {

    private int statusCode;

    public NetworkClientException(Throwable th) {
        super(th);
    }

    public NetworkClientException(Throwable th, int statusCode) {
        super("[Status Code - "+statusCode+"]: " + th.getMessage(), th);
        this.statusCode = statusCode;
    }

    public NetworkClientException(String message, int statusCode) {
        super("[Status Code - "+statusCode+"]: " + message);
        this.statusCode = statusCode;
    }
}
