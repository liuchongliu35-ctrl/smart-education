package com.bing.tpa.Thread.pptVideoThread.model;

import java.util.Objects;

import com.bing.tpa.Thread.pptVideoThread.sdk.JSON;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * SplitRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class SplitRequest {
    public static final String SERIALIZED_NAME_DOCUMENT = "document";
    @SerializedName(SERIALIZED_NAME_DOCUMENT)
    private File document;

    public static final String SERIALIZED_NAME_OPTIONS = "options";
    @SerializedName(SERIALIZED_NAME_OPTIONS)
    private SplitOptions options;

    public SplitRequest() {
    }

    public SplitRequest document(File document) {

        this.document = document;
        return this;
    }

    /**
     * Get document
     * @return document
     **/
    @javax.annotation.Nullable
    public File getDocument() {
        return document;
    }


    public void setDocument(File document) {
        this.document = document;
    }


    public SplitRequest options(SplitOptions options) {

        this.options = options;
        return this;
    }

    /**
     * Get options
     * @return options
     **/
    @javax.annotation.Nullable
    public SplitOptions getOptions() {
        return options;
    }


    public void setOptions(SplitOptions options) {
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
        SplitRequest splitRequest = (SplitRequest) o;
        return Objects.equals(this.document, splitRequest.document) &&
                Objects.equals(this.options, splitRequest.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, options);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SplitRequest {\n");
        sb.append("    document: ").append(toIndentedString(document)).append("\n");
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
        openapiFields.add("document");
        openapiFields.add("options");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to SplitRequest
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!SplitRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in SplitRequest is not found in the empty JSON string", SplitRequest.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!SplitRequest.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `SplitRequest` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
        // validate the optional field `options`
        if (jsonObj.get("options") != null && !jsonObj.get("options").isJsonNull()) {
            SplitOptions.validateJsonObject(jsonObj.getAsJsonObject("options"));
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!SplitRequest.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'SplitRequest' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<SplitRequest> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(SplitRequest.class));

            return (TypeAdapter<T>) new TypeAdapter<SplitRequest>() {
                @Override
                public void write(JsonWriter out, SplitRequest value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public SplitRequest read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of SplitRequest given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of SplitRequest
     * @throws IOException if the JSON string is invalid with respect to SplitRequest
     */
    public static SplitRequest fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, SplitRequest.class);
    }

    /**
     * Convert an instance of SplitRequest to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}