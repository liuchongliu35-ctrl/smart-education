package com.bing.tpa.domain.VO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class PointLink{
    private Integer pointId;
    private String title;
    private String parentSecondTitle;//可以作为教学设计的designTitle
    private Integer level;
    private Integer RelationType;
    private List<ChildPoints> children;

    @Data
    @Setter
    @Getter
    public class ChildPoints{
        private Integer pointId;
        private String title;
        private String childSecondTitle;//可以作为教学设计的secondaryTitle
        private Integer level;
        private Integer RelationType;
    }
}
