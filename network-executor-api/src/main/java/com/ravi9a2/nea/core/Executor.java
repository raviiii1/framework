package com.ravi9a2.nea.core;

import com.ravi9a2.nca.Client;

/**
 * A generic interface that wraps a Client and provides the APIs to execute a call to a down-stream.
 * The interface aims to be a generic interface for Reactive and Non-reactive versions of Executor
 * and extracts the boilerplate code for making a resilient call to a downstream via the Client.
 *
 * @param <C> The client.
 * @author raviprakash
 */
public interface Executor<C extends Client> {
}