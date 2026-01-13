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
 * ImageWatermarkOptions
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class ImageWatermarkOptions {
    public static final String SERIALIZED_NAME_ANGLE = "angle";
    @SerializedName(SERIALIZED_NAME_ANGLE)
    private Integer angle;

    public static final String SERIALIZED_NAME_ZOOM = "zoom";
    @SerializedName(SERIALIZED_NAME_ZOOM)
    private Integer zoom;

    public ImageWatermarkOptions() {
    }

    public ImageWatermarkOptions angle(Integer angle) {

        this.angle = angle;
        return this;
    }

    /**
     * Get angle
     * @return angle
     **/
    @javax.annotation.Nullable
    public Integer getAngle() {
        return angle;
    }


    public void setAngle(Integer angle) {
        this.angle = angle;
    }


    public ImageWatermarkOptions zoom(Integer zoom) {

        this.zoom = zoom;
        return this;
    }

    /**
     * Get zoom
     * @return zoom
     **/
    @javax.annotation.Nullable
    public Integer getZoom() {
        return zoom;
    }


    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageWatermarkOptions imageWatermarkOptions = (ImageWatermarkOptions) o;
        return Objects.equals(this.angle, imageWatermarkOptions.angle) &&
                Objects.equals(this.zoom, imageWatermarkOptions.zoom);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(angle, zoom);
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
        sb.append("class ImageWatermarkOptions {\n");
        sb.append("    angle: ").append(toIndentedString(angle)).append("\n");
        sb.append("    zoom: ").append(toIndentedString(zoom)).append("\n");
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
        openapiFields.add("angle");
        openapiFields.add("zoom");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to ImageWatermarkOptions
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!ImageWatermarkOptions.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in ImageWatermarkOptions is not found in the empty JSON string", ImageWatermarkOptions.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!ImageWatermarkOptions.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `ImageWatermarkOptions` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!ImageWatermarkOptions.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'ImageWatermarkOptions' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<ImageWatermarkOptions> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(ImageWatermarkOptions.class));

            return (TypeAdapter<T>) new TypeAdapter<ImageWatermarkOptions>() {
                @Override
                public void write(JsonWriter out, ImageWatermarkOptions value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public ImageWatermarkOptions read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of ImageWatermarkOptions given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of ImageWatermarkOptions
     * @throws IOException if the JSON string is invalid with respect to ImageWatermarkOptions
     */
    public static ImageWatermarkOptions fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, ImageWatermarkOptions.class);
    }

    /**
     * Convert an instance of ImageWatermarkOptions to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}
