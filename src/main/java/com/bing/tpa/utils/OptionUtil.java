package com.bing.tpa.utils;

import com.bing.tpa.exception.FormatException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//处理选项格式的，将选项的字符串格式转为数组形式，同时将选项除去
public class OptionUtil {

    public static List<String> optionDeal(String options) throws FormatException {
//        String[] optionArray = options.split("[;；\\s]+");
        String regex = "[A-D]\\.\\s*.*?(?=[A-D]\\.\\s*|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(options);

        List<String> optionList = new ArrayList<>();
        while (matcher.find()) {
            optionList.add(matcher.group().trim().replaceAll("^[A-D]\\.", "").replace("\\",""));
        }
        return optionList;
    }
}
