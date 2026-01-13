package com.bing.tpa.Thread.pptVideoThread.api;
import com.bing.tpa.Thread.pptVideoThread.model.VideoOptions;
import com.bing.tpa.Thread.pptVideoThread.sdk.*;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;

public class SlidizeApi {

    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public SlidizeApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SlidizeApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }
    public int getHostIndex() {
        return localHostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.localHostIndex = hostIndex;
    }

    public String getCustomBaseUrl() {
        return localCustomBaseUrl;
    }

    public void setCustomBaseUrl(String customBaseUrl) {
        this.localCustomBaseUrl = customBaseUrl;
    }


    public File convertToVideo(File document, VideoOptions options) throws ApiException {
        ApiResponse<File> localVarResp = convertToVideoWithHttpInfo(document, options);
        return localVarResp.getData();
    }

    public ApiResponse<File> convertToVideoWithHttpInfo(File document, VideoOptions options) throws ApiException {
        okhttp3.Call localVarCall = convertToVideoValidateBeforeCall(document, options, null);
        Type localVarReturnType = new TypeToken<File>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call convertToVideoValidateBeforeCall(File document, VideoOptions options, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'document' is set
        if (document == null) {
            throw new ApiException("Missing the required parameter 'document' when calling convertToVideo(Async)");
        }

        return convertToVideoCall(document, options, _callback);

    }

    public okhttp3.Call convertToVideoCall(File document, VideoOptions options, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/video";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        if (document != null) {
            localVarFormParams.put("document", document);
        }

        if (options != null) {
            localVarFormParams.put("options", options);
        }

        final String[] localVarAccepts = {
                "text/plain",
                "application/json",
                "text/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
                "multipart/form-data"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

}
