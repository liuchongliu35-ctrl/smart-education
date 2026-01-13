package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class TaskResourceVo {
//    预习资料
    private String readResource;
//    预习题目
    private List<String> question;
}
