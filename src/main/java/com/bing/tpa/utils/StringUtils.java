package com.bing.tpa.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// TODO 弃用这个旧版知识点处理工具
@Component
public class StringUtils {

//  TODO 弃用parseSyllabus方法

//    public  List<TpaSubjectSyllabus> parseSyllabus(String knowledgeText, Integer tsId) {
//        if (tsId == null) {
//            throw new NullPointerException("学科的id不可以为null");
//        }
//
//        // 移除 "=" 符号
//        String newString = knowledgeText.replace("=", "");
//
//        // 使用 "%" 分割单元和知识点部分
//        String[] pointSections = newString.split("%");
//
//        List<TpaSubjectSyllabus> resultPoints = new ArrayList<>();
//
//        for (int i = 0; i < pointSections.length; i++) {
//            if (i % 2 != 0) { // 奇数位置包含知识点
//                // 获取单元标题（前一个偶数位置）
//                String unitTitle = pointSections[i - 1].trim();
//
//                // 分割知识点
//                String[] points = pointSections[i].split("\n");
//
//                for (String point : points) {
//                    String trimmedPoint = point.trim();
//                    if (!trimmedPoint.isEmpty()) { // 跳过空行
//                        TpaSubjectSyllabus syllabus = new TpaSubjectSyllabus();
//                        syllabus.setTsId(tsId);
//                        syllabus.setTopTitle(unitTitle); // 设置单元标题
//                        syllabus.setSecondaryTitle(trimmedPoint); // 设置知识点
//                        resultPoints.add(syllabus);
//                    }
//                }
//            }
//        }
//
//        return resultPoints;
//    }
}
/**
 * TpaSubjectSyllabus syllabus = new TpaSubjectSyllabus();
 *             if(tsId==null) throw new NullPointerException("学科的id不可可以为null");
 *             syllabus.setTsId(tsId);
 */