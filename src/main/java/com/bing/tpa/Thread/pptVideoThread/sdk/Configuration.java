package com.bing.tpa.Thread.pptVideoThread.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class Configuration {
    public static final String VERSION = "24.6.0";

    private static ApiClient defaultApiClient = new ApiClient();

    /**
     * Get the default API client, which would be used when creating API
     * instances without providing an API client.
     *
     * @return Default API client
     */
    public static ApiClient getDefaultApiClient() {
        File configFile = new File("apiConfig.json");
        String path = configFile.getAbsolutePath();
        if (configFile.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = null;
            try {
                jsonNode = objectMapper.readTree(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String basePath = jsonNode.get("basePath").asText();
            if(basePath != null && !basePath.trim().isEmpty())
            {
                defaultApiClient.setBasePath(basePath); //read from JSON
                return defaultApiClient;
            }

        }
        return defaultApiClient;
    }

    /**
     * Set the default API client, which would be used when creating API
     * instances without providing an API client.
     *
     * @param apiClient API client
     */
    public static void setDefaultApiClient(ApiClient apiClient) {
        defaultApiClient = apiClient;
    }
}
