package com.bing.tpa.domain.digital;

import io.swagger.models.auth.In;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BaseSetupInfo {
    private Integer digitalMotion;
    private Boolean enhancer;
    private MultipartFile video;
    private String gender;
}
