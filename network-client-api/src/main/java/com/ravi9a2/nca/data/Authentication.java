package com.ravi9a2.nca.data;

import java.util.Objects;

/**
 * A pojo representing the Authentication header for a Client
 *
 * @author raviiii1
 */
public class Authentication {
    protected String authKey;
    protected String authSecret;
    protected String secondAuthKey;
    protected String secondAuthSecret;

    private Authentication(String authKey, String authSecret) {
        this.authKey = authKey;
        this.authSecret = authSecret;
    }

    public String getAuthKey() {
        return authKey;
    }

    public String getAuthSecret() {
        return authSecret;
    }

    public String getSecondAuthKey() {
        return secondAuthKey;
    }

    public String getSecondAuthSecret() {
        return secondAuthSecret;
    }

    private void setSecondAuthKey(String secondAuthKey) {
        this.secondAuthKey = secondAuthKey;
    }

    private void setSecondAuthSecret(String secondAuthSecret) {
        this.secondAuthSecret = secondAuthSecret;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String authKey;
        private String authSecret;
        private String secondAuthKey;
        private String secondAuthSecret;

        public Builder authKey(String authKey) {
            this.authKey = authKey;
            return this;
        }

        public Builder authSecret(String authSecret) {
            this.authSecret = authSecret;
            return this;
        }

        public Builder secondAuthKey(String secondAuthKey) {
            this.secondAuthKey = secondAuthKey;
            return this;
        }

        public Builder secondAuthSecret(String secondAuthSecret) {
            this.secondAuthSecret = secondAuthSecret;
            return this;
        }

        public Authentication build() {
            Authentication authentication = new Authentication(this.authKey, this.authSecret);
            if(Objects.nonNull(secondAuthKey) && Objects.nonNull(secondAuthSecret)) {
                authentication.setSecondAuthKey(secondAuthKey);
                authentication.setSecondAuthSecret(secondAuthSecret);
            }
            return authentication;
        }
    }
}
