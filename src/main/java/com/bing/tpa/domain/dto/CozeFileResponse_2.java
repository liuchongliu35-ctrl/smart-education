package com.bing.tpa.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CozeFileResponse_2 {
    private int code;
    @JsonProperty("detail")
    private Detail detail;
    @JsonProperty("document_infos")
    private List<DocumentInfo> document_infos;
    private String msg;

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public List<DocumentInfo> getDocument_infos() {
        return document_infos;
    }

    public void setDocument_infos(List<DocumentInfo> document_infos) {
        this.document_infos = document_infos;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    // Nested classes
    public static class DocumentInfo {
        private int char_count;
        private ChunkStrategy chunk_strategy;
        private int create_time;
        private String document_id;
        private int format_type;
        private int hit_count;
        private String name;
        private int size;
        private int slice_count;
        private int source_type;
        private int status;
        private String type;
        private int update_interval;
        private int update_time;
        private int update_type;

        // Getters and Setters
        public int getChar_count() {
            return char_count;
        }

        public void setChar_count(int char_count) {
            this.char_count = char_count;
        }

        public ChunkStrategy getChunk_strategy() {
            return chunk_strategy;
        }

        public void setChunk_strategy(ChunkStrategy chunk_strategy) {
            this.chunk_strategy = chunk_strategy;
        }

        public int getCreate_time() {
            return create_time;
        }

        public void setCreate_time(int create_time) {
            this.create_time = create_time;
        }

        public String getDocument_id() {
            return document_id;
        }

        public void setDocument_id(String document_id) {
            this.document_id = document_id;
        }

        public int getFormat_type() {
            return format_type;
        }

        public void setFormat_type(int format_type) {
            this.format_type = format_type;
        }

        public int getHit_count() {
            return hit_count;
        }

        public void setHit_count(int hit_count) {
            this.hit_count = hit_count;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getSlice_count() {
            return slice_count;
        }

        public void setSlice_count(int slice_count) {
            this.slice_count = slice_count;
        }

        public int getSource_type() {
            return source_type;
        }

        public void setSource_type(int source_type) {
            this.source_type = source_type;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getUpdate_interval() {
            return update_interval;
        }

        public void setUpdate_interval(int update_interval) {
            this.update_interval = update_interval;
        }

        public int getUpdate_time() {
            return update_time;
        }

        public void setUpdate_time(int update_time) {
            this.update_time = update_time;
        }

        public int getUpdate_type() {
            return update_type;
        }

        public void setUpdate_type(int update_type) {
            this.update_type = update_type;
        }
    }

    public static class ChunkStrategy {
        private int chunk_type;
        private String separator;
        private long max_tokens;
        private boolean remove_extra_spaces;
        private boolean remove_urls_emails;
        private int caption_type;

        // Getters and Setters
        public int getChunk_type() {
            return chunk_type;
        }

        public void setChunk_type(int chunk_type) {
            this.chunk_type = chunk_type;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public long getMax_tokens() {
            return max_tokens;
        }

        public void setMax_tokens(long max_tokens) {
            this.max_tokens = max_tokens;
        }

        public boolean isRemove_extra_spaces() {
            return remove_extra_spaces;
        }

        public void setRemove_extra_spaces(boolean remove_extra_spaces) {
            this.remove_extra_spaces = remove_extra_spaces;
        }

        public boolean isRemove_urls_emails() {
            return remove_urls_emails;
        }

        public void setRemove_urls_emails(boolean remove_urls_emails) {
            this.remove_urls_emails = remove_urls_emails;
        }

        public int getCaption_type() {
            return caption_type;
        }

        public void setCaption_type(int caption_type) {
            this.caption_type = caption_type;
        }
    }

    public static class Detail {
        @JsonProperty("logid")
        private String log_id;

        // Getter and Setter
        public String getLog_id() {
            return log_id;
        }

        public void setLog_id(String log_id) {
            this.log_id = log_id;
        }
    }
}