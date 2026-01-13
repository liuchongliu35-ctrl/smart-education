package com.bing.tpa.Thread.pptVideoThread.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;

/**
 * Gets or Sets VideoTransitionType
 */
@Getter
@JsonAdapter(VideoTransitionType.Adapter.class)
public enum VideoTransitionType {

    NONE("None"),

    RANDOM("Random"),

    FROMPRESENTATION("FromPresentation"),

    FADE("Fade"),

    DISTANCE("Distance"),

    SLIDELEFT("SlideLeft"),

    CIRCLECROP("CircleCrop"),

    DISSOLVE("Dissolve");

    private String value;

    VideoTransitionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static VideoTransitionType fromValue(String value) {
        for (VideoTransitionType b : VideoTransitionType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<VideoTransitionType> {
        @Override
        public void write(final JsonWriter jsonWriter, final VideoTransitionType enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public VideoTransitionType read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return VideoTransitionType.fromValue(value);
        }
    }
}