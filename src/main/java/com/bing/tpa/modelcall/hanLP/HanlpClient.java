package com.bing.tpa.modelcall.hanLP;

import com.hankcs.hanlp.restful.HanLPClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class HanlpClient {
//Nzg3NUBiYnMuaGFubHAuY29tOmRJeEF0UzhUNEdQWnZCUUQ=
//    67d2cecceaf6d8f49fa24d21
//    通过浏览器访问API文档时的用户名为： 7875@bbs.hanlp.com
//密码为： dIxAtS8T4GPZvBQD
    private static final HanLPClient hanLp=new HanLPClient("https://www.hanlp.com/hanlp/v21/redirect","67d2cecceaf6d8f49fa24d21","zh",25);
    public Map<String, Double> getKeywords(String msg) {
        Map<String, Double> extraction = null;
        try {
            extraction = hanLp.keyphraseExtraction(msg, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extraction;
    }
}
