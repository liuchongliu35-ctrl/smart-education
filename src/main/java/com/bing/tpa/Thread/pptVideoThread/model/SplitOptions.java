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
 * SplitOptions
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class SplitOptions {
    public static final String SERIALIZED_NAME_SLIDES_RANGE = "slidesRange";
    @SerializedName(SERIALIZED_NAME_SLIDES_RANGE)
    private String slidesRange;

    public SplitOptions() {
    }

    public SplitOptions slidesRange(String slidesRange) {

        this.slidesRange = slidesRange;
        return this;
    }

    /**
     * Get slidesRange
     * @return slidesRange
     **/
    @javax.annotation.Nullable
    public String getSlidesRange() {
        return slidesRange;
    }


    public void setSlidesRange(String slidesRange) {
        this.slidesRange = slidesRange;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SplitOptions splitOptions = (SplitOptions) o;
        return Objects.equals(this.slidesRange, splitOptions.slidesRange);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(slidesRange);
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
        sb.append("class SplitOptions {\n");
        sb.append("    slidesRange: ").append(toIndentedString(slidesRange)).append("\n");
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
        openapiFields.add("slidesRange");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to SplitOptions
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!SplitOptions.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in SplitOptions is not found in the empty JSON string", SplitOptions.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!SplitOptions.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `SplitOptions` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
        if ((jsonObj.get("slidesRange") != null && !jsonObj.get("slidesRange").isJsonNull()) && !jsonObj.get("slidesRange").isJsonPrimitive()) {
            throw new IllegalArgumentException(String.format("Expected the field `slidesRange` to be a primitive type in the JSON string but got `%s`", jsonObj.get("slidesRange").toString()));
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!SplitOptions.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'SplitOptions' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<SplitOptions> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(SplitOptions.class));

            return (TypeAdapter<T>) new TypeAdapter<SplitOptions>() {
                @Override
                public void write(JsonWriter out, SplitOptions value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public SplitOptions read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of SplitOptions given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of SplitOptions
     * @throws IOException if the JSON string is invalid with respect to SplitOptions
     */
    public static SplitOptions fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, SplitOptions.class);
    }

    /**
     * Convert an instance of SplitOptions to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}
