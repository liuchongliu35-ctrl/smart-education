package com.bing.tpa.Thread.pptVideoThread;

import com.bing.tpa.Thread.pptVideoThread.api.SlidizeApi;
import com.bing.tpa.Thread.pptVideoThread.model.VideoOptions;
import com.bing.tpa.Thread.pptVideoThread.model.VideoResolutionType;
import com.bing.tpa.Thread.pptVideoThread.model.VideoTransitionType;
import com.bing.tpa.Thread.pptVideoThread.sdk.ApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConvertToVideoThread {
    private final SlidizeApi api = new SlidizeApi();

    public void convertToVideo(String path,String outPath,Integer time) throws ApiException, IOException {
        File file = new File(path);

        VideoOptions options = new VideoOptions();
        options.setDuration(time);
        options.setTransition(1);
        options.setTransitionType(VideoTransitionType.DISSOLVE);
        options.setResolutionType(VideoResolutionType.FULLHD);

        File response = api.convertToVideo(file, options);
        Path target = Paths.get(outPath);

        // 确保目标目录存在
        Path parentDir = target.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        // 复制文件到指定位置
        Files.copy(
                response.toPath(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

}
