package com.ravi9a2.nca;

import com.ravi9a2.nca.data.ClientConfig;

/**
 * Builder for the Client type.
 *
 * @author raviiii1
 */
public interface ClientBuilder<C extends Client> {

    /**
     * Builds a Client from ClientConfig.
     *
     * @param config the ClientConfig.
     * @return the Client.
     */
    C build(ClientConfig config);

    /**
     * Builds a Client.
     *
     * @return the Client.
     */
    C build();
}
