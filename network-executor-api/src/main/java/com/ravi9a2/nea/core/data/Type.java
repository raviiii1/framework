package com.ravi9a2.nea.core.data;

/**
 * Enum for call type. This supports only HTTP for now,
 * but can be extended to other types.
 *
 * @author raviprakash
 */
public enum Type {
    HTTP("HTTP"),
    RPC("RPC");

    String value;

    Type(String val) {
        this.value = val;
    }
}
