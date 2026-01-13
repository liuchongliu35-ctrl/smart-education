package com.bing.tpa.utils;
import com.spire.presentation.*;
import org.springframework.stereotype.Component;

//todo 该工具类用于检查ppt是否每一页都有批注
@Component
public class RemarkCheckUtil {

    public boolean check(String pptPath){
        Presentation ppt = null;
        try {
            // 1. 加载PPT文件
            ppt = new Presentation();
            ppt.loadFromFile(pptPath);

            // 2. 获取所有幻灯片
            int slideCount = ppt.getSlides().size();

            // 3. 遍历每一页检查批注
            for (int slideIndex = 0; slideIndex < slideCount; slideIndex++) {
                ISlide slide = ppt.getSlides().get(slideIndex);
                Comment[] comments = slide.getComments();

                // 4. 判断当前页是否有批注
                if (comments == null || comments.length == 0) {
                    // 发现无批注的页面，返回false
                    return false;
                }
            }
            // 5. 所有页都有批注
            return true;

        } catch (Exception e) {
            // 异常处理：记录日志并返回false
            System.err.println("检查PPT批注时出错: " + e.getMessage());
            return false;
        } finally {
            // 6. 确保释放资源
            if (ppt != null) {
                ppt.dispose();
            }
        }
    }
}
