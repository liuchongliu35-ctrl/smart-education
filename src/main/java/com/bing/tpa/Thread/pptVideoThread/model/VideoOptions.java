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
 * VideoOptions
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-06-11T23:12:27.361+05:00[Asia/Yekaterinburg]")
public class VideoOptions {
    public static final String SERIALIZED_NAME_DURATION = "duration";
    @SerializedName(SERIALIZED_NAME_DURATION)
    private Integer duration;

    public static final String SERIALIZED_NAME_TRANSITION = "transition";
    @SerializedName(SERIALIZED_NAME_TRANSITION)
    private Integer transition;

    public static final String SERIALIZED_NAME_TRANSITION_TYPE = "transitionType";
    @SerializedName(SERIALIZED_NAME_TRANSITION_TYPE)
    private VideoTransitionType transitionType;

    public static final String SERIALIZED_NAME_RESOLUTION_TYPE = "resolutionType";
    @SerializedName(SERIALIZED_NAME_RESOLUTION_TYPE)
    private VideoResolutionType resolutionType;

    public VideoOptions() {
    }

    public VideoOptions duration(Integer duration) {

        this.duration = duration;
        return this;
    }

    /**
     * Get duration
     * @return duration
     **/
    @javax.annotation.Nullable
    public Integer getDuration() {
        return duration;
    }


    public void setDuration(Integer duration) {
        this.duration = duration;
    }


    public VideoOptions transition(Integer transition) {

        this.transition = transition;
        return this;
    }

    /**
     * Get transition
     * @return transition
     **/
    @javax.annotation.Nullable
    public Integer getTransition() {
        return transition;
    }


    public void setTransition(Integer transition) {
        this.transition = transition;
    }


    public VideoOptions transitionType(VideoTransitionType transitionType) {

        this.transitionType = transitionType;
        return this;
    }

    /**
     * Get transitionType
     * @return transitionType
     **/
    @javax.annotation.Nullable
    public VideoTransitionType getTransitionType() {
        return transitionType;
    }


    public void setTransitionType(VideoTransitionType transitionType) {
        this.transitionType = transitionType;
    }


    public VideoOptions resolutionType(VideoResolutionType resolutionType) {

        this.resolutionType = resolutionType;
        return this;
    }

    /**
     * Get resolutionType
     * @return resolutionType
     **/
    @javax.annotation.Nullable
    public VideoResolutionType getResolutionType() {
        return resolutionType;
    }


    public void setResolutionType(VideoResolutionType resolutionType) {
        this.resolutionType = resolutionType;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VideoOptions videoOptions = (VideoOptions) o;
        return Objects.equals(this.duration, videoOptions.duration) &&
                Objects.equals(this.transition, videoOptions.transition) &&
                Objects.equals(this.transitionType, videoOptions.transitionType) &&
                Objects.equals(this.resolutionType, videoOptions.resolutionType);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, transition, transitionType, resolutionType);
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
        sb.append("class VideoOptions {\n");
        sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
        sb.append("    transition: ").append(toIndentedString(transition)).append("\n");
        sb.append("    transitionType: ").append(toIndentedString(transitionType)).append("\n");
        sb.append("    resolutionType: ").append(toIndentedString(resolutionType)).append("\n");
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
        openapiFields.add("duration");
        openapiFields.add("transition");
        openapiFields.add("transitionType");
        openapiFields.add("resolutionType");

        // a set of required properties/fields (JSON key names)
        openapiRequiredFields = new HashSet<String>();
    }

    /**
     * Validates the JSON Object and throws an exception if issues found
     *
     * @param jsonObj JSON Object
     * @throws IOException if the JSON Object is invalid with respect to VideoOptions
     */
    public static void validateJsonObject(JsonObject jsonObj) throws IOException {
        if (jsonObj == null) {
            if (!VideoOptions.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
                throw new IllegalArgumentException(String.format("The required field(s) %s in VideoOptions is not found in the empty JSON string", VideoOptions.openapiRequiredFields.toString()));
            }
        }

        Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
        // check to see if the JSON string contains additional fields
        for (Entry<String, JsonElement> entry : entries) {
            if (!VideoOptions.openapiFields.contains(entry.getKey())) {
                throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `VideoOptions` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
            }
        }
    }

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!VideoOptions.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'VideoOptions' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<VideoOptions> thisAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(VideoOptions.class));

            return (TypeAdapter<T>) new TypeAdapter<VideoOptions>() {
                @Override
                public void write(JsonWriter out, VideoOptions value) throws IOException {
                    JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
                    elementAdapter.write(out, obj);
                }

                @Override
                public VideoOptions read(JsonReader in) throws IOException {
                    JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
                    validateJsonObject(jsonObj);
                    return thisAdapter.fromJsonTree(jsonObj);
                }

            }.nullSafe();
        }
    }

    /**
     * Create an instance of VideoOptions given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of VideoOptions
     * @throws IOException if the JSON string is invalid with respect to VideoOptions
     */
    public static VideoOptions fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, VideoOptions.class);
    }

    /**
     * Convert an instance of VideoOptions to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}