package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TpaHomeworkTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Mapper
public interface TpaHomeworkDetailsMapper extends BaseMapper<TpaHomeworkDetails> {

    List<TpaHomeworkDetails> findQuestionBath(@Param("hids") List<Integer> hids);

    List<TpaHomeworkDetails> selectPreviewQuestions(@Param("taskIds") List<Integer> taskIds);

    List<TpaHomeworkTrack> selectPreviewQuestionsAnswer(@Param("ptId") List<Integer> ptId);

    Integer updateReviewTimeInteger(@Param("hid") Integer hid, @Param("time") LocalDateTime time);

    List<TpaHomeworkDetails> selectByKeyword(@Param("keyword") String keyword);

    List<TpaHomeworkDetails>  selectQuestionBySubject(@Param("subject") String subject, @Param("stage") String stage);

    List<TpaHomeworkDetails> getQuestionBySubject(@Param("subject") String subject,  @Param("stage")String stage);

    List<TpaHomeworkDetails> getQuestionByPtId(@Param("ptId") Integer ptId);

    List<TpaHomeworkDetails> getQuestionByHId(@Param("hid") Integer hid);

    List<TpaHomeworkDetails> selectPtQuestion(@Param("ptId") Integer ptId);

    List<TpaHomeworkDetails> selectSpecial(@Param("uid") Integer uid,@Param("ptId") Integer ptId);

    List<TpaHomeworkDetails> selectQuestions(@Param("hid") Integer hid,
                                          @Param("ptId") Integer ptId);
}
