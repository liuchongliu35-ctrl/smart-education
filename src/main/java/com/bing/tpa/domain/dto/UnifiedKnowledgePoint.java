package com.bing.tpa.domain.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UnifiedKnowledgePoint {
    private Integer id;          // 知识点ID（模板ID或自定义ID）
    private String source;       // 来源: 'template' 或 'custom'
    private String topTitle;
    private String secondaryTitle;
    private String content;
    private Integer level;
    private Integer schoolId;    // 所属学校ID（自定义知识点特有）
    private Integer tsId;        // 所属课程ID
    private Integer templateId;
    // 关联的模板ID（如果是自定义知识点）
//    @JsonBackReference("child-parent")
    private List<UnifiedKnowledgePoint> children = new ArrayList<>(); // 子知识点列表
//    @JsonManagedReference("child-parent")
//    @JsonIgnoreProperties("children")
    private List<UnifiedKnowledgePoint> parents = new ArrayList<>();

    public void addChild(UnifiedKnowledgePoint child) {
        this.children.add(child);
    }
    // 修改setter方法
    public void setId(Integer id) {
        this.id = id;
        // 如果是模板知识点，templateId与id相同
        if ("template".equals(this.source)) {
            this.templateId = id;
        }
    }
}
