package com.bing.tpa.domain.digital;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Setter
@Getter
public class Config {
    public boolean hasAction;
    public boolean faceEnhance;
    public boolean actionEnhance;
    public String voice;
    public boolean aiAnnotation;
    private int tdId;
}
//    public MultipartFile pptFile;
//    public MultipartFile avatar;
