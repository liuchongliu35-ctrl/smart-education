package com.bing.tpa.service.baseImpl;

import com.bing.tpa.common.ResourceType;
import com.bing.tpa.domain.VO.DesignAndPPTVo;
import com.bing.tpa.domain.VO.PPTVo;
import com.bing.tpa.domain.digital.VideoListVo;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.mapper.TpaTeachDesignMapper;
import com.bing.tpa.service.baseService.DigitalService;
import com.bing.tpa.service.baseService.TpaTeachDesignService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.FileSizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DigitalServiceImpl implements DigitalService {

    @Autowired
    private TpaTeachDesignService designService;
    @Autowired
    private TpaTeacherService teacherService;
    @Autowired
    private TpaTeachDesignMapper designMapper;
    @Autowired
    private ResourceService resource;
    private final String mergeVideoRootPath="mergeVideo";
    @Override
    public List<VideoListVo> getVideoList() throws IOException {

//      根据当前用户获取该用户的所有教学设计，ppt，视频数据
        TpaTeacher user = teacherService.getCurrentUser();
        List<VideoListVo> videoListVos = new ArrayList<>();
        List<DesignAndPPTVo> designAndPPT = designService.getAllDesignAndPPT();
        int idCount=1;
        for (DesignAndPPTVo designAndPPTVo : designAndPPT) {
            VideoListVo videoListVo = new VideoListVo();
            videoListVo.setVideoWithPPTName(designAndPPTVo.getPptName());
            videoListVo.setVideoWithPPTUrl(designAndPPTVo.getPptUrl());
//            根据教学设计的名字和uid和教学设计id获取属于当前这个教学设计的视频
//            构建视频路径
            getVideoInfo(user, designAndPPTVo, videoListVo);
            videoListVo.setVideoDesc(designAndPPTVo.getContent());
            videoListVo.setVideoSize(FileSizeUtil.getSize(resource.getResourcePath(ResourceType.VIDEO,videoListVo.getVideoUrl())));
            videoListVo.setVid(idCount);
            idCount++;
            videoListVos.add(videoListVo);
        }
        return videoListVos;
    }

    @Override
    public List<VideoListVo> getVideoByKnowledge(String title) throws IOException {
        TpaTeacher user = teacherService.getCurrentUser();
        List<TpaTeachDesign> designs = designMapper.selectByTitle(title);
        List<DesignAndPPTVo> designAndPPT = designService.getAllDesignAndPPT();
        List<VideoListVo> videoListVos = new ArrayList<>();
        int idCount=1;
        for (TpaTeachDesign design: designs) {
            VideoListVo videoListVo = new VideoListVo();
//            根据tdId从designAndPPT中查找数据
            Optional<DesignAndPPTVo> resultOptional = designAndPPT.stream()
                    .filter(item -> Objects.equals(item.getTdId(), design.getTdId()))
                    .findFirst();
//            根据查找到的数据进行收集
            resultOptional.ifPresent(item -> {
                videoListVo.setVideoWithPPTName(item.getPptName());
                videoListVo.setVideoWithPPTUrl(item.getPptUrl());
                videoListVo.setVideoDesc(item.getContent());
            });
//            查找与该教学设计相关的视频
            getVideoInfo(user, design, videoListVo);
            videoListVo.setVid(idCount);
            idCount++;
            videoListVos.add(videoListVo);
        }
        return videoListVos;
    }

    private void getVideoInfo(TpaTeacher user, TpaTeachDesign design, VideoListVo videoListVo) throws IOException {
//        先构建用户视频目录
        Path videoRoot= Paths.get(user.getAccount());
//        构建视频名字
        String videoName=user.getUid()+"--"+design.getTdId()+"--"+design.getDesignName()+".mp4";
        Path videoPath = videoRoot.resolve(videoName);
        videoListVo.setIsHasVideo(false);
//        需要判断该视频文件是否存在
        if (resource.existsResource(ResourceType.VIDEO,videoPath.toString())) {
            String videoUrl = videoPath.toString().replace(File.separator, "/");
            videoListVo.setVideoUrl(videoUrl);
            videoListVo.setVideoName(videoName);
            videoListVo.setVideoSize(FileSizeUtil.getSize(resource.getResourcePath(ResourceType.VIDEO,videoUrl)));
            videoListVo.setIsHasVideo(true);
        }
    }
}
