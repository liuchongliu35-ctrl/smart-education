package com.bing.tpa.Thread.pptVideoThread.model;

import java.util.Objects;
import java.util.Arrays;

import com.bing.tpa.Thread.pptVideoThread.sdk.JSON;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.openapitools.jackson.nullable.JsonNullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * ProtectionOptions
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class ProtectionOptions {
    public static final String SERIALIZED_NAME_VIEW_PASSWORD = "viewPassword";
    @SerializedName(SERIALIZED_NAME_VIEW_PASSWORD)
    private String viewPassword;

    public static final String SERIALIZED_NAME_EDIT_PASSWORD = "editPassword";
    @SerializedName(SERIALIZED_NAME_EDIT_PASSWORD)
    private String editPassword;

    public static final String SERIALIZED_NAME_MARK_AS_FINAL = "markAsFinal";
    @SerializedName(SERIALIZED_NAME_MARK_AS_FINAL)
    private Boolean markAsFinal;

    public ProtectionOptions() {
    }

    public ProtectionOptions viewPassword(String viewPassword) {

        this.viewPassword = viewPassword;
        return this;
    }

    /**
     * Get viewPassword
     * @return viewPassword
     **/
    @javax.annotation.Nullable
    public String getViewPassword() {
        return viewPassword;
    }


    public void setViewPassword(String viewPassword) {
        this.viewPassword = viewPassword;
    }


    public ProtectionOptions editPassword(String editPassword) {

        this.editPassword = editPassword;
        return this;
    }

    /**
     * Get editPassword
     * @return editPassword
     **/
    @javax.annotation.Nullable
    public String getEditPassword() {
        return editPassword;
    }


    public void setEditPassword(String editPassword) {
        this.editPassword = editPassword;
    }


    public ProtectionOptions markAsFinal(Boolean markAsFinal) {

        this.markAsFinal = markAsFinal;
        return this;
    }

    /**
     * Get markAsFinal
     * @return markAsFinal
     **/
    @javax.annotation.Nullable
    public Boolean getMarkAsFinal() {
        return markAsFinal;
    }


    public void setMarkAsFinal(Boolean markAsFinal) {
        this.markAsFinal = markAsFinal;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtectionOptions protectionOptions = (ProtectionOptions) o;
        return Objects.equals(this.viewPassword, protectionOptions.viewPassword) &&
                Objects.equals(this.editPassword, protectionOptions.editPassword) &&
                Objects.equals(this.markAsFinal, protectionOptions.markAsFinal);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewPassword, editPassword, markAsFinal);
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a) {
        if (a == null) {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProtectionOptions {\n");
        sb.append("    viewPassword: ").append(toIndentedString(viewPassword)).append("\n");
        sb.append("    editPassword: ").append(toIndentedString(editPassword)).append("\n");
        sb.append("    markAsFinal: ").append(toIndentedString(markAsFinal)).append("\n");
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
        openapiFields.add("viewPassword");
        openapiFields.add("editPassword");
        openapiFields.add("markAsFinal");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to ProtectionOptions
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!ProtectionOptions.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in ProtectionOptions is not found in the empty JSON string", ProtectionOptions.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!ProtectionOptions.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `ProtectionOptions` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
        if ((jsonObj.get("viewPassword") != null && !jsonObj.get("viewPassword").isJsonNull()) && !jsonObj.get("viewPassword").isJsonPrimitive()) {
            throw new IllegalArgumentException(String.format("Expected the field `viewPassword` to be a primitive type in the JSON string but got `%s`", jsonObj.get("viewPassword").toString()));
        }
        if ((jsonObj.get("editPassword") != null && !jsonObj.get("editPassword").isJsonNull()) && !jsonObj.get("editPassword").isJsonPrimitive()) {
            throw new IllegalArgumentException(String.format("Expected the field `editPassword` to be a primitive type in the JSON string but got `%s`", jsonObj.get("editPassword").toString()));
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!ProtectionOptions.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'ProtectionOptions' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<ProtectionOptions> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(ProtectionOptions.class));

            return (TypeAdapter<T>) new TypeAdapter<ProtectionOptions>() {
                @Override
                public void write(JsonWriter out, ProtectionOptions value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public ProtectionOptions read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of ProtectionOptions given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of ProtectionOptions
     * @throws IOException if the JSON string is invalid with respect to ProtectionOptions
     */
    public static ProtectionOptions fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, ProtectionOptions.class);
    }

    /**
     * Convert an instance of ProtectionOptions to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}
