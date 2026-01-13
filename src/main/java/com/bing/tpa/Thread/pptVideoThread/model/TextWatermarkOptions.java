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
 * TextWatermarkOptions
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class TextWatermarkOptions {
    public static final String SERIALIZED_NAME_TEXT = "text";
    @SerializedName(SERIALIZED_NAME_TEXT)
    private String text;

    public static final String SERIALIZED_NAME_FONT_NAME = "fontName";
    @SerializedName(SERIALIZED_NAME_FONT_NAME)
    private String fontName;

    public static final String SERIALIZED_NAME_FONT_SIZE = "fontSize";
    @SerializedName(SERIALIZED_NAME_FONT_SIZE)
    private Integer fontSize;

    public static final String SERIALIZED_NAME_COLOR = "color";
    @SerializedName(SERIALIZED_NAME_COLOR)
    private String color;

    public static final String SERIALIZED_NAME_ANGLE = "angle";
    @SerializedName(SERIALIZED_NAME_ANGLE)
    private Integer angle;

    public TextWatermarkOptions() {
    }

    public TextWatermarkOptions text(String text) {

        this.text = text;
        return this;
    }

    /**
     * Get text
     * @return text
     **/
    @javax.annotation.Nullable
    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }


    public TextWatermarkOptions fontName(String fontName) {

        this.fontName = fontName;
        return this;
    }

    /**
     * Get fontName
     * @return fontName
     **/
    @javax.annotation.Nullable
    public String getFontName() {
        return fontName;
    }


    public void setFontName(String fontName) {
        this.fontName = fontName;
    }


    public TextWatermarkOptions fontSize(Integer fontSize) {

        this.fontSize = fontSize;
        return this;
    }

    /**
     * Get fontSize
     * @return fontSize
     **/
    @javax.annotation.Nullable
    public Integer getFontSize() {
        return fontSize;
    }


    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }


    public TextWatermarkOptions color(String color) {

        this.color = color;
        return this;
    }

    /**
     * Get color
     * @return color
     **/
    @javax.annotation.Nullable
    public String getColor() {
        return color;
    }


    public void setColor(String color) {
        this.color = color;
    }


    public TextWatermarkOptions angle(Integer angle) {

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



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TextWatermarkOptions textWatermarkOptions = (TextWatermarkOptions) o;
        return Objects.equals(this.text, textWatermarkOptions.text) &&
                Objects.equals(this.fontName, textWatermarkOptions.fontName) &&
                Objects.equals(this.fontSize, textWatermarkOptions.fontSize) &&
                Objects.equals(this.color, textWatermarkOptions.color) &&
                Objects.equals(this.angle, textWatermarkOptions.angle);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, fontName, fontSize, color, angle);
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
        sb.append("class TextWatermarkOptions {\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("    fontName: ").append(toIndentedString(fontName)).append("\n");
        sb.append("    fontSize: ").append(toIndentedString(fontSize)).append("\n");
        sb.append("    color: ").append(toIndentedString(color)).append("\n");
        sb.append("    angle: ").append(toIndentedString(angle)).append("\n");
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
        openapiFields.add("text");
        openapiFields.add("fontName");
        openapiFields.add("fontSize");
        openapiFields.add("color");
        openapiFields.add("angle");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to TextWatermarkOptions
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!TextWatermarkOptions.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in TextWatermarkOptions is not found in the empty JSON string", TextWatermarkOptions.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!TextWatermarkOptions.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `TextWatermarkOptions` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
        if ((jsonObj.get("text") != null && !jsonObj.get("text").isJsonNull()) && !jsonObj.get("text").isJsonPrimitive()) {
            throw new IllegalArgumentException(String.format("Expected the field `text` to be a primitive type in the JSON string but got `%s`", jsonObj.get("text").toString()));
        }
        if ((jsonObj.get("fontName") != null && !jsonObj.get("fontName").isJsonNull()) && !jsonObj.get("fontName").isJsonPrimitive()) {
            throw new IllegalArgumentException(String.format("Expected the field `fontName` to be a primitive type in the JSON string but got `%s`", jsonObj.get("fontName").toString()));
        }
        if ((jsonObj.get("color") != null && !jsonObj.get("color").isJsonNull()) && !jsonObj.get("color").isJsonPrimitive()) {
            throw new IllegalArgumentException(String.format("Expected the field `color` to be a primitive type in the JSON string but got `%s`", jsonObj.get("color").toString()));
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!TextWatermarkOptions.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'TextWatermarkOptions' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<TextWatermarkOptions> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(TextWatermarkOptions.class));

            return (TypeAdapter<T>) new TypeAdapter<TextWatermarkOptions>() {
                @Override
                public void write(JsonWriter out, TextWatermarkOptions value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public TextWatermarkOptions read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of TextWatermarkOptions given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of TextWatermarkOptions
     * @throws IOException if the JSON string is invalid with respect to TextWatermarkOptions
     */
    public static TextWatermarkOptions fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, TextWatermarkOptions.class);
    }

    /**
     * Convert an instance of TextWatermarkOptions to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}
