package com.bing.tpa.Thread.pptVideoThread.auth;

import com.bing.tpa.Thread.pptVideoThread.sdk.ApiException;
import com.bing.tpa.Thread.pptVideoThread.sdk.Pair;

import java.net.URI;
import java.util.Map;
import java.util.List;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class HttpBearerAuth implements Authentication {
    private final String scheme;
    private String bearerToken;

    public HttpBearerAuth(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the token, which together with the scheme, will be sent as the value of the Authorization header.
     *
     * @return The bearer token
     */
    public String getBearerToken() {
        return bearerToken;
    }

    /**
     * Sets the token, which together with the scheme, will be sent as the value of the Authorization header.
     *
     * @param bearerToken The bearer token to send in the Authorization header
     */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public void applyToParams(List<Pair> queryParams, Map<String, String> headerParams, Map<String, String> cookieParams,
                              String payload, String method, URI uri) throws ApiException {
        if (bearerToken == null) {
            return;
        }

        headerParams.put("Authorization", (scheme != null ? upperCaseBearer(scheme) + " " : "") + bearerToken);
    }

    private static String upperCaseBearer(String scheme) {
        return ("bearer".equalsIgnoreCase(scheme)) ? "Bearer" : scheme;
    }
}
