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
 * Gets or Sets VideoResolutionType
 */
@Getter
@JsonAdapter(VideoResolutionType.Adapter.class)
public enum VideoResolutionType {

    SD("SD"),

    HD("HD"),

    FULLHD("FullHD"),

    QHD("QHD");

    private String value;

    VideoResolutionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static VideoResolutionType fromValue(String value) {
        for (VideoResolutionType b : VideoResolutionType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<VideoResolutionType> {
        @Override
        public void write(final JsonWriter jsonWriter, final VideoResolutionType enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public VideoResolutionType read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return VideoResolutionType.fromValue(value);
        }
    }
}