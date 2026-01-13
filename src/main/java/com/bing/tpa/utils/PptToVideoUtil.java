package com.bing.tpa.utils;

import com.aspose.slides.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PptToVideoUtil {
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

//    @Async("taskExecutor")
    public void pptUtilsAsync(String pptPath, String mp4Path, String durationsJson) {
        pptUtils(pptPath, mp4Path, durationsJson);
    }

    public static void pptUtils(String pptPath, String mp4Path, String durationsJson) {
        String frameFolder = "F:\\tpa_system\\src\\main\\resources\\frame\\";
        Presentation presentation = null;
        ExecutorService executor = null;

        try {
            long startTime = System.currentTimeMillis();
            System.out.println("开始加载PPT文件: " + pptPath);
            presentation = new Presentation(pptPath);

            // 解析JSON获取每页时长
            Map<Integer, Float> pageDurations = parseDurationsJson(durationsJson);
            System.out.println("成功解析JSON时长数据: " + pageDurations.size() + "页");

            // 为每页设置动画时长
            for (int i = 0; i < presentation.getSlides().size(); i++) {
                ISlide slide = presentation.getSlides().get_Item(i);
                ISequence mainSequence = slide.getTimeline().getMainSequence();

                float duration = pageDurations.getOrDefault(i, 2.0f);

                IAutoShape shape = slide.getShapes().addAutoShape(ShapeType.Rectangle, 0, 0, 1, 1);
                shape.getFillFormat().setFillType(FillType.NoFill);
                shape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
                shape.setHidden(true);

                IEffect effect = mainSequence.addEffect(shape, EffectType.Fade,
                        EffectSubtype.None, EffectTriggerType.AfterPrevious);

                effect.getTiming().setDuration(duration);

                System.out.printf("设置幻灯片 %d 的时长为: %.2f秒%n", i, duration);
            }

            final int fps = 33;
            System.out.println("设置帧率为: " + fps + " FPS");

            // 2. 使用单线程队列处理帧保存（避免Aspose线程安全问题）
            executor = Executors.newSingleThreadExecutor();
            CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

            List<String> frames = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger frameCounter = new AtomicInteger(0);

            // 3. 使用JPG格式替代PNG
            final String imageFormat = "JPG";

            PresentationAnimationsGenerator animationsGenerator = new PresentationAnimationsGenerator(presentation);
            try {
                PresentationPlayer player = new PresentationPlayer(animationsGenerator, fps);
                try {
                    System.out.println("开始生成动画帧 (单线程处理)...");
                    player.setFrameTick((sender, arguments) -> {
                        try {
                            // 4. 在主线程中获取帧数据（避免多线程问题）
                            int frameIndex = sender.getFrameIndex();
                            final BufferedImage[] image = {arguments.getFrame()};

                            // 5. 提交到单线程队列处理
                            completionService.submit(() -> {
                                try {
                                    // 确保帧目录存在
                                    File dir = new File(frameFolder);
                                    if (!dir.exists()) dir.mkdirs();

                                    String frameFile = frameFolder + String.format("frame_%04d." + imageFormat.toLowerCase(), frameIndex);

                                    // 6. 降低分辨率（可选）
                                    if (image[0].getWidth() > 1920) {
                                        double scaleFactor = 1920.0 / image[0].getWidth();
                                        int newWidth = 1920;
                                        int newHeight = (int) (image[0].getHeight() * scaleFactor);
                                        image[0] = resizeImage(image[0], newWidth, newHeight);
                                    }

                                    // 7. 使用JPG替代PNG
                                    ImageIO.write(image[0], "JPEG", new File(frameFile));

                                    int currentCount = frameCounter.incrementAndGet();
                                    if (currentCount % 50 == 0) {
                                        System.out.println("已生成 " + currentCount + " 帧");
                                    }
                                    return frameFile;
                                } catch (IOException e) {
                                    System.err.println("保存帧时出错: " + e.getMessage());
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            System.err.println("获取帧数据时出错: " + e.getMessage());
                        }
                    });

                    animationsGenerator.run(presentation.getSlides());
                    System.out.println("完成动画帧生成，共生成 " + frameCounter.get() + " 帧");

                    // 收集所有帧文件
                    for (int i = 0; i < frameCounter.get(); i++) {
                        Future<String> future = completionService.take();
                        String frameFile = future.get();
                        if (frameFile != null) {
                            frames.add(frameFile);
                        }
                    }
                } finally {
                    if (player != null) player.dispose();
                }
            } finally {
                if (animationsGenerator != null) animationsGenerator.dispose();
            }

            // 配置FFmpeg
            String ffmpegPath = "C:\\Users\\LC335\\ffmpeg\\ffmpeg-full\\bin\\ffmpeg.exe";
            String ffprobePath = "C:\\Users\\LC335\\ffmpeg\\ffmpeg-full\\bin\\ffprobe.exe";

            // 检查FFmpeg路径
            if (!new File(ffmpegPath).exists()) {
                throw new IOException("FFmpeg可执行文件不存在: " + ffmpegPath);
            }
            if (!new File(ffprobePath).exists()) {
                throw new IOException("FFprobe可执行文件不存在: " + ffprobePath);
            }

            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFprobe ffprobe = new FFprobe(ffprobePath);

            // 构建高清视频转换命令 - 优化速度
            System.out.println("开始FFmpeg视频编码...");
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(new File(frameFolder, "frame_%04d.jpg").getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(mp4Path)
                    .setFormat("mp4")
                    // 8. 尝试硬件加速编码
                    .setVideoCodec("libx264") // 优先使用CPU编码
                    // 备用选项:
                    // .setVideoCodec("h264_nvenc") // NVIDIA GPU加速
                    // .setVideoCodec("h264_amf")  // AMD GPU加速
                    .setVideoFrameRate(fps)
                    .setVideoBitRate(2000_000) // 适当降低码率
                    // 9. 使用更快的预设
                    .setPreset("fast")
                    .addExtraArgs(
                            "-pix_fmt", "yuv420p",
                            "-movflags", "+faststart",
                            "-profile:v", "main", // 使用main profile提高兼容性
                            "-crf", "23",         // 适当提高CRF值（23是默认值）
                            // 10. 多线程编码
                            "-threads", String.valueOf(THREAD_POOL_SIZE)
                    )
                    .done();

            FFmpegExecutor ffmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe);
            ffmpegExecutor.createJob(builder).run();
            System.out.println("FFmpeg视频转换完成");

            // 清理临时帧文件
            System.out.println("清理临时帧文件...");
            int deletedCount = 0;
            for (String frame : frames) {
                if (new File(frame).delete()) {
                    deletedCount++;
                }
            }
            System.out.println("已删除 " + deletedCount + "/" + frames.size() + " 个临时帧文件");

            long endTime = System.currentTimeMillis();
            System.out.println("视频转换完成，耗时: " + (endTime - startTime)/1000 + "秒");

        } catch (Exception e) {
            System.err.println("处理过程中发生错误: ");
            e.printStackTrace();
        } finally {
            // 关闭线程池
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
            }

            // 释放资源
            if (presentation != null) {
                presentation.dispose();
            }
        }
    }

    // 图像缩放方法
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        java.awt.Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
    // 解析JSON数据的方法
    private static Map<Integer, Float> parseDurationsJson(String durationsJson) {
        if (durationsJson == null || durationsJson.isEmpty()) {
            return new HashMap<>();
        }

        Type type = new TypeToken<Map<String, Float>>(){}.getType();
        Map<String, Float> tempMap = new Gson().fromJson(durationsJson, type);

        Map<Integer, Float> durationsMap = new HashMap<>();
        for (Map.Entry<String, Float> entry : tempMap.entrySet()) {
            try {
                int slideIndex = Integer.parseInt(entry.getKey());
                durationsMap.put(slideIndex, entry.getValue());
            } catch (NumberFormatException e) {
                System.err.println("无法解析页码: " + entry.getKey());
            }
        }
        return durationsMap;
    }
}

