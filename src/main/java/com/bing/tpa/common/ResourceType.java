package com.bing.tpa.common;

public enum ResourceType {
    PPT("pptFile", "application/vnd.ms-powerpoint"),
    VIDEO("mergeVideo", "video/mp4"),
    IMAGE("images", "image/jpeg"),
    DOCUMENT("documents", "application/msword"),
    SPLITPPT("splitPPTFile","application/vnd.ms-powerpoint"),
    TeacherVideo("teacherImageVideo", "video/mp4"),
    SPLITVIDEO("splitPPTVideoFile","video/mp4"),
    VIDEOFILE("videoFile","video/mp4"),
    JSON("json", "application/json"),
    WORD("wordFile", "application/vnd.ms-word"),
    PPTPHOTO("pptPhoto","image/jpeg");
    private final String directory;
    private final String defaultMimeType;

    ResourceType(String directory, String defaultMimeType) {
        this.directory = directory;
        this.defaultMimeType = defaultMimeType;
    }

    public String getDirectory() {
        return directory;
    }

    public String getDefaultMimeType() {
        return defaultMimeType;
    }
}
