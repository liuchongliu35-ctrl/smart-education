package com.bing.tpa.domain.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaHomeworkTrack;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrackWithDetails extends TpaHomeworkTrack {
    @TableField(exist = false)
    private TpaHomeworkDetails homeworkDetails;
}
