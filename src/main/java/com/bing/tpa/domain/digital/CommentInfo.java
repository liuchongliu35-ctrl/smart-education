package com.bing.tpa.domain.digital;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.awt.geom.Point2D;
import java.util.Date;

@Data
@Getter
@Setter
public class CommentInfo {
    private final String author;
    private final String text;
    private final Point2D.Float position;
    private final Date time;
}
