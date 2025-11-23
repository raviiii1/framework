package com.ravi9a2.nca.exceptions;

/**
 * All the exceptions due to any timeouts should be wrapped
 * in {@code TimeoutException}
 *
 * @author raviiii1
 */
public class TimeoutException extends NetworkClientException {

    public TimeoutException(Throwable th) {
        super(th, 504);
    }
}
