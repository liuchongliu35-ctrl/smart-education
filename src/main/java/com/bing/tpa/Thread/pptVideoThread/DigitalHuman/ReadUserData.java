package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

import com.bing.tpa.domain.digital.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ReadUserData {
    private static final String USER_CONFIG_PATH = "src/main/resources/account/user.json";
//    读取用户json数据
    public List<LoginRequest> readUserConfigs(int pptPageNum) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(USER_CONFIG_PATH);

        // 读取所有用户配置
        Map<String, String> userMap = mapper.readValue(configFile, Map.class);

        // 2. 将Map转换为List<LoginRequest>
        List<LoginRequest> userList = new ArrayList<>();
        for (Map.Entry<String, String> entry : userMap.entrySet()) {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(entry.getKey()); // 用户名（Map的键）
            loginRequest.setPassword(entry.getValue()); // 密码（Map的值）
            userList.add(loginRequest);
        }

        // 检查用户数量是否足够
        if (userList.size() < pptPageNum) {
            throw new RuntimeException(String.format(
                    "需要 %d 个用户，但配置文件中只有 %d 个用户",
                    pptPageNum, userList.size()));
        }
        // 返回所需数量的用户配置
        return userList.subList(0, pptPageNum);
    }

}
