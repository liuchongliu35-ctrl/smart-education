package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class PhotoVo {
    private String  key;
    private List<String> photoUrl;
}
