package com.bing.tpa.Thread.pptVideoThread.model;

import java.util.Objects;

import com.bing.tpa.Thread.pptVideoThread.sdk.JSON;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * MergeRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class MergeRequest {
    public static final String SERIALIZED_NAME_DOCUMENTS = "documents";
    @SerializedName(SERIALIZED_NAME_DOCUMENTS)
    private List<File> documents;

    public static final String SERIALIZED_NAME_OPTIONS = "options";
    @SerializedName(SERIALIZED_NAME_OPTIONS)
    private MergeOptions options;

    public MergeRequest() {
    }

    public MergeRequest documents(List<File> documents) {

        this.documents = documents;
        return this;
    }

    public MergeRequest addDocumentsItem(File documentsItem) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(documentsItem);
        return this;
    }

    /**
     * Get documents
     * @return documents
     **/
    @javax.annotation.Nullable
    public List<File> getDocuments() {
        return documents;
    }


    public void setDocuments(List<File> documents) {
        this.documents = documents;
    }


    public MergeRequest options(MergeOptions options) {

        this.options = options;
        return this;
    }

    /**
     * Get options
     * @return options
     **/
    @javax.annotation.Nullable
    public MergeOptions getOptions() {
        return options;
    }


    public void setOptions(MergeOptions options) {
        this.options = options;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MergeRequest mergeRequest = (MergeRequest) o;
        return Objects.equals(this.documents, mergeRequest.documents) &&
                Objects.equals(this.options, mergeRequest.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documents, options);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MergeRequest {\n");
        sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }


    public static HashSet<String> openapiFields;
    public static HashSet<String> openapiRequiredFields;

    static {
        // a set of all properties/fields (JSON key names)
        openapiFields = new HashSet<String>();
        openapiFields.add("documents");
        openapiFields.add("options");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to MergeRequest
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!MergeRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in MergeRequest is not found in the empty JSON string", MergeRequest.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!MergeRequest.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `MergeRequest` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
        // ensure the optional json data is an array if present
        if (jsonObj.get("documents") != null && !jsonObj.get("documents").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `documents` to be an array in the JSON string but got `%s`", jsonObj.get("documents").toString()));
        }
        // validate the optional field `options`
        if (jsonObj.get("options") != null && !jsonObj.get("options").isJsonNull()) {
            MergeOptions.validateJsonObject(jsonObj.getAsJsonObject("options"));
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!MergeRequest.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'MergeRequest' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<MergeRequest> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(MergeRequest.class));

            return (TypeAdapter<T>) new TypeAdapter<MergeRequest>() {
                @Override
                public void write(JsonWriter out, MergeRequest value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public MergeRequest read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of MergeRequest given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of MergeRequest
     * @throws IOException if the JSON string is invalid with respect to MergeRequest
     */
    public static MergeRequest fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, MergeRequest.class);
    }

    /**
     * Convert an instance of MergeRequest to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}
